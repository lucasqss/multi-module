package com.example.pagamento.entrypoint;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de INTEGRAÇÃO: a aplicação EMPACOTADA (o jar de {@code mvn verify}) sobe como um processo separado,
 * contra o Oracle do Dev Services, e é exercitada só por HTTP.
 *
 * <ul>
 *   <li>Herda todos os cenários de {@link PagamentoResourceTest} (padrão do Quarkus): o mesmo teste caixa-preta
 *       roda no {@code @QuarkusTest} (rápido, na JVM do teste) e aqui (como em produção), sem duplicar código.</li>
 *   <li>Não há CDI: nada de {@code @Inject} — o teste não está no mesmo processo da aplicação.</li>
 *   <li>Profile: {@code quarkus.test.integration-test-profile=test} (application.properties).</li>
 *   <li>Nome terminado em {@code IT}: roda pelo maven-failsafe-plugin em {@code mvn verify}, não em {@code mvn test}.</li>
 * </ul>
 *
 * Abaixo, só o que faz sentido com o app empacotado e requisições reais em paralelo.
 */
@QuarkusIntegrationTest
class PagamentoResourceIT extends PagamentoResourceTest {

    /** Lock pessimista + idempotência com o banco real: o mesmo aviso chegando 5 vezes ao mesmo tempo. */
    @Test
    void retornosSimultaneosDoBancoSaoAplicadosUmaVezSo() throws Exception {
        String id = criar("INDIVIDUAL");
        String transacao = enviado(id, linha(1, 10000));

        Callable<Integer> aviso = () -> bancoRetorna(transacao, "LIQUIDADO", 10000L).extract().statusCode();
        try (var paralelo = Executors.newFixedThreadPool(5)) {
            var respostas = paralelo.invokeAll(IntStream.range(0, 5).mapToObj(i -> aviso).toList());
            for (var resposta : respostas) {
                assertEquals(200, resposta.get()); // cada um espera o lock do anterior e vê o pagamento já pago
            }
        }

        given().get(API + "/" + id).then().statusCode(200)
                .body("status", equalTo("PAGO"))
                .body("transacao.autenticacao", equalTo("AUT-" + transacao))
                .body("transacao.envios", equalTo(1));
    }
}
