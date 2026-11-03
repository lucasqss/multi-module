package com.example.consorcio.entrypoint;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/** Teste ponta a ponta: entrypoint -> use case -> domínio -> adapters. */
@QuarkusTest
class ProcessoPagamentoResourceTest {

    private static final String PATH = "/api/v1/processos-pagamento";

    private String body(int cota, String valor, String forma, String extra) {
        return """
                {"grupo":1234,"cota":%d,"valor":%s,"formaPagamento":"%s","dataVencimento":"%s"%s}
                """.formatted(cota, valor, forma, LocalDate.now().plusDays(5), extra);
    }

    @Test
    void criaConsultaENaoPermiteDuplicado() {
        String id = given().contentType(ContentType.JSON)
                .body(body(10, "1500.00", "DEBITO_EM_CONTA", ",\"agenciaDebito\":\"1234\",\"contaDebito\":\"998877\""))
                .when().post(PATH)
                .then().statusCode(201)
                .header("Location", notNullValue())
                .body("status", equalTo("ABERTO"))
                .body("cota", equalTo("001234-0010"))
                .extract().path("id");

        given().when().get(PATH + "/" + id)
                .then().statusCode(200)
                .body("valor", equalTo(1500.00f));

        given().contentType(ContentType.JSON)
                .body(body(10, "100.00", "PIX", ""))
                .when().post(PATH)
                .then().statusCode(422)
                .body("codigo", equalTo("PROCESSO_JA_EM_ABERTO"));
    }

    @Test
    void cotaCanceladaRetorna422() {
        given().contentType(ContentType.JSON)
                .body(body(12, "100.00", "PIX", ""))
                .when().post(PATH)
                .then().statusCode(422)
                .body("codigo", equalTo("COTA_NAO_ACEITA_PAGAMENTO"));
    }

    @Test
    void cotaInexistenteRetorna404() {
        given().contentType(ContentType.JSON)
                .body(body(999, "100.00", "PIX", ""))
                .when().post(PATH)
                .then().statusCode(404);
    }

    @Test
    void campoObrigatorioAusenteRetorna400() {
        given().contentType(ContentType.JSON)
                .body("{\"grupo\":1234,\"cota\":10}")
                .when().post(PATH)
                .then().statusCode(400);
    }
}
