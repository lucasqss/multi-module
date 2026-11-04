package com.example.pagamento.adapter;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.exception.RecursoBloqueadoException;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.PagamentoId;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Lock PESSIMISTA: enquanto a transação A tem o pagamento travado ({@code SELECT ... FOR UPDATE}),
 * uma segunda operação de alteração espera; esgotado o tempo, recebe {@link RecursoBloqueadoException} (409).
 * Leitura sem lock não espera.
 */
@QuarkusTest
class LockPessimistaTest {

    @Inject
    PagamentoRepository pagamentos;

    @Inject
    Clock clock;

    private PagamentoId novoPagamento() {
        return QuarkusTransaction.requiringNew().call(() -> {
            var p = Pagamento.criar(Modalidade.INDIVIDUAL, clock.instant());
            pagamentos.salvar(p);
            return p.getId();
        });
    }

    @Test
    void segundaAlteracaoNoMesmoPagamentoEsperaEDesiste() {
        var id = novoPagamento();

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
        var id = novoPagamento();

        QuarkusTransaction.requiringNew().run(() -> {
            pagamentos.obterParaAlteracao(id); // outra operação em andamento

            given().contentType("application/json").body("{\"motivo\":\"desisti\"}")
                    .post("/api/v1/pagamentos/" + id.valor() + "/cancelamento")
                    .then().statusCode(409).body("codigo", equalTo("RECURSO_BLOQUEADO"));
        });
    }
}
