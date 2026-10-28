package com.example.processo;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/** O que o deploy usa além da API: health para o orquestrador e o contrato OpenAPI. Mesmos recursos dos outros ITs (o jar sobe uma vez só). */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
@WithTestResource(CadastroFalso.class)
class OperacaoIT {

    @Test
    void prontoComOBancoNoAr() {
        given().get("/q/health/ready").then().statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void contratoOpenApiTrazOsEndpoints() {
        given().get("/q/openapi").then().statusCode(200)
                .body(containsString("/processos/imobiliarios"))
                .body(containsString("/processos/{id}/processamento"));
    }
}
