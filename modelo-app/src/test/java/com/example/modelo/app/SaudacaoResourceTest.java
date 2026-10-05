package com.example.modelo.app;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/** Sobe o Quarkus em memória: prova que o bean do modelo-core foi descoberto e injetado. */
@QuarkusTest
class SaudacaoResourceTest {

    @Test
    void saudaPeloNome() {
        given().when().get("/saudacoes/Ana")
                .then().statusCode(200)
                .body("nome", equalTo("Ana"))
                .body("mensagem", equalTo("Olá, Ana!"));
    }

    @Test
    void nomeEmBrancoRespondeBadRequest() {
        given().pathParam("nome", " ").when().get("/saudacoes/{nome}")
                .then().statusCode(400)
                .body("motivo", equalTo("nome é obrigatório"));
    }
}
