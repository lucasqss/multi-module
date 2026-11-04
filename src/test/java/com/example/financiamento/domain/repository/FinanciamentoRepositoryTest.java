package com.example.financiamento.domain.repository;

import com.example.financiamento.adapter.registro.RegistroDeGarantias;
import com.example.financiamento.application.financiamento.ExecutarComandoFinanciamentoUC;
import com.example.financiamento.application.financiamento.comando.ContratarFinanciamento;
import com.example.financiamento.domain.Cenarios;
import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.exception.RecursoBloqueadoException;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persistência e transação, contra o Oracle do Dev Services:
 * <ul>
 *   <li>lock PESSIMISTA: enquanto A tem o financiamento travado ({@code SELECT ... FOR UPDATE}), a alteração de B
 *       espera e, esgotado o tempo, recebe {@link RecursoBloqueadoException} (409); leitura sem lock não espera;</li>
 *   <li>listagem sem N+1 (documentos carregados em lote);</li>
 *   <li>efeito externo do comando só depois do commit.</li>
 * </ul>
 */
@QuarkusTest
class FinanciamentoRepositoryTest {

    @Inject
    FinanciamentoRepository financiamentos;

    @Inject
    ExecutarComandoFinanciamentoUC executar;

    @Inject
    RegistroDeGarantias registro;

    @Inject
    SessionFactory sessionFactory;

    @Inject
    Clock clock;

    private UUID gravar(Supplier<Financiamento> financiamento) {
        return QuarkusTransaction.requiringNew().call(() -> {
            var f = financiamento.get();
            financiamentos.salvar(f);
            return f.getId();
        });
    }

    // ============================== lock pessimista ==============================

    @Test
    void segundaAlteracaoNoMesmoFinanciamentoEsperaEDesiste() {
        var id = gravar(() -> Financiamento.criar(TipoFinanciamento.IMOVEL, clock.instant()));

        QuarkusTransaction.requiringNew().run(() -> {
            financiamentos.obterParaAlteracao(id); // A trava

            assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> financiamentos.obter(id))); // leitura: ok
            assertThrows(RecursoBloqueadoException.class,
                    () -> QuarkusTransaction.requiringNew().run(() -> financiamentos.obterParaAlteracao(id))); // B: bloqueado
        });

        assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> financiamentos.obterParaAlteracao(id))); // A terminou
    }

    @Test
    void naApiOBloqueioViraConflito409() {
        var id = gravar(() -> Financiamento.criar(TipoFinanciamento.VEICULO, clock.instant()));

        QuarkusTransaction.requiringNew().run(() -> {
            financiamentos.obterParaAlteracao(id); // outra operação em andamento

            given().contentType("application/json").body("{\"motivo\":\"desisti\"}")
                    .post("/api/v1/financiamentos/" + id + "/cancelamento")
                    .then().statusCode(409).body("codigo", equalTo("RECURSO_BLOQUEADO"));
        });
    }

    // ============================== consultas ==============================

    /** Página maior que o lote padrão do Quarkus (16): sem {@code @BatchSize(100)} seriam 2 lotes de documentos. */
    @Test
    void listarCarregaOsDocumentosDaPaginaNumaConsultaSo() {
        for (int i = 0; i < 20; i++) {
            gravar(() -> Cenarios.imovel().rascunho());
        }
        var estatisticas = sessionFactory.getStatistics();

        QuarkusTransaction.requiringNew().run(() -> {
            estatisticas.clear();
            var pagina = financiamentos.listar(TipoFinanciamento.IMOVEL, null, 0, 20);

            assertEquals(20, pagina.itens().size());
            pagina.itens().forEach(f -> f.getDocumentos().documentos().size()); // força carregar a coleção de cada um
            // página + count + UMA consulta com os documentos dos 20 (N+1 puro seriam 22)
            assertTrue(estatisticas.getPrepareStatementCount() <= 3,
                    "consultas: " + estatisticas.getPrepareStatementCount());
        });
    }

    // ============================== efeito após o commit ==============================

    @Test
    void envioParaRegistroSoAconteceSeATransacaoConfirmar() {
        var id = gravar(() -> Cenarios.veiculo().aprovado());
        var protocolo = id + "-1";

        assertThrows(IllegalStateException.class, () -> QuarkusTransaction.requiringNew().run(() -> {
            executar.executar(id, new ContratarFinanciamento()); // participa desta transação...
            throw new IllegalStateException("falha depois do comando"); // ...que é desfeita
        }));
        assertFalse(registro.envios().stream().anyMatch(e -> e.idProtocolo().equals(protocolo)));

        executar.executar(id, new ContratarFinanciamento()); // transação própria: confirma e envia
        assertTrue(registro.envios().stream().anyMatch(e -> e.idProtocolo().equals(protocolo)));
    }
}
