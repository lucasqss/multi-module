package com.example.generator;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class StringGeneratorResourceTest {

    @Test
    void crudDoOutroModuloEGeradorFuncionamJuntos() {
        given().contentType("application/json")
                .body("{\"nome\":\"Lapis\",\"descricao\":\"Preto\"}")
                .post("/produtos")
                .then().statusCode(201);

        given().get("/gerador")
                .then().statusCode(200)
                .body(is("CanetaAzulCadernoPautadoLapisPreto"));
    }
}
