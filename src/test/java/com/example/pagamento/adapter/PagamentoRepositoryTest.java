package com.example.pagamento.adapter;

import com.example.pagamento.adapter.banco.BancoPagamentos;
import com.example.pagamento.adapter.database.FiltroPagamento;
import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.application.pagamento.ExecutarComandoPagamentoUC;
import com.example.pagamento.application.pagamento.comando.EnviarAoBanco;
import com.example.pagamento.domain.Cenarios;
import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.exception.RecursoBloqueadoException;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.PagamentoId;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistência e transação, contra o H2:
 * <ul>
 *   <li>lock PESSIMISTA: enquanto A tem o pagamento travado ({@code SELECT ... FOR UPDATE}), a alteração de B
 *       espera e, esgotado o tempo, recebe {@link RecursoBloqueadoException} (409); leitura sem lock não espera;</li>
 *   <li>listagem sem N+1 (boletos carregados em lote);</li>
 *   <li>efeito externo do comando só depois do commit.</li>
 * </ul>
 */
@QuarkusTest
class PagamentoRepositoryTest {

    @Inject
    PagamentoRepository pagamentos;

    @Inject
    ExecutarComandoPagamentoUC executar;

    @Inject
    BancoPagamentos banco;

    @Inject
    SessionFactory sessionFactory;

    @Inject
    Clock clock;

    private PagamentoId gravar(Supplier<Pagamento> pagamento) {
        return QuarkusTransaction.requiringNew().call(() -> {
            var p = pagamento.get();
            pagamentos.salvar(p);
            return p.getId();
        });
    }

    // ============================== lock pessimista ==============================

    @Test
    void segundaAlteracaoNoMesmoPagamentoEsperaEDesiste() {
        var id = gravar(() -> Pagamento.criar(Modalidade.INDIVIDUAL, clock.instant()));

        QuarkusTransaction.requiringNew().run(() -> {
            pagamentos.obterParaAlteracao(id); // A trava

            assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> pagamentos.obter(id))); // leitura: ok
            assertThrows(RecursoBloqueadoException.class,
                    () -> QuarkusTransaction.requiringNew().run(() -> pagamentos.obterParaAlteracao(id))); // B: bloqueado
        });

        assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> pagamentos.obterParaAlteracao(id))); // A terminou
    }

    @Test
    void naApiOBloqueioViraConflito409() {
        var id = gravar(() -> Pagamento.criar(Modalidade.INDIVIDUAL, clock.instant()));

        QuarkusTransaction.requiringNew().run(() -> {
            pagamentos.obterParaAlteracao(id); // outra operação em andamento

            given().contentType("application/json").body("{\"motivo\":\"desisti\"}")
                    .post("/api/v1/pagamentos/" + id.valor() + "/cancelamento")
                    .then().statusCode(409).body("codigo", equalTo("RECURSO_BLOQUEADO"));
        });
    }

    // ============================== consultas ==============================

    /** Página maior que o lote padrão do Quarkus (16): sem {@code @BatchSize(100)} seriam 2 lotes de boletos. */
    @Test
    void listarCarregaOsBoletosDaPaginaNumaConsultaSo() {
        for (int i = 0; i < 20; i++) {
            gravar(() -> Cenarios.lote().rascunho());
        }
        var estatisticas = sessionFactory.getStatistics();

        QuarkusTransaction.requiringNew().run(() -> {
            estatisticas.clear();
            var pagina = pagamentos.listar(new FiltroPagamento(Modalidade.LOTE, null), 0, 20);

            assertEquals(20, pagina.itens().size());
            assertTrue(pagina.itens().stream().allMatch(p -> p.getBoletos().quantidade() == 2));
            // página + count + UMA consulta com os boletos dos 20 (N+1 puro seriam 22)
            assertTrue(estatisticas.getPrepareStatementCount() <= 3,
                    "consultas: " + estatisticas.getPrepareStatementCount());
        });
    }

    // ============================== efeito após o commit ==============================

    @Test
    void envioAoBancoSoAconteceSeATransacaoConfirmar() {
        var id = gravar(() -> Cenarios.individual().agendado());
        var transacao = id + "-1";

        assertThrows(IllegalStateException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            executar.executar(id.valor(), new EnviarAoBanco()); // participa desta transação...
            throw new IllegalStateException("falha depois do comando"); // ...que é desfeita
        }));
        assertFalse(banco.envios().stream().anyMatch(e -> e.idTransacao().equals(transacao)));

        executar.executar(id.valor(), new EnviarAoBanco()); // transação própria: confirma e envia
        assertTrue(banco.envios().stream().anyMatch(e -> e.idTransacao().equals(transacao)));
    }
}
