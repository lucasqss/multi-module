package com.example.processo;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/** O CRUD simples de ponta a ponta, contra o jar e o Oracle. Mesmos recursos dos outros ITs: o jar sobe uma vez só. */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
@WithTestResource(CadastroFalso.class)
class AgenciasIT {

    static String codigo() {
        return Integer.toString(ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    static void criar(String codigo) {
        given().contentType(ContentType.JSON).body(Map.of("codigo", codigo, "nome", "Centro", "uf", "SP"))
                .post("/agencias").then().statusCode(201)
                .header("Location", endsWith("/agencias/" + codigo));
    }

    @Test
    void cicloCompleto() {
        var codigo = codigo();
        criar(codigo);

        given().get("/agencias/" + codigo).then().statusCode(200)
                .body("nome", equalTo("Centro")).body("uf", equalTo("SP")).body("ativa", equalTo(true));
        given().get("/agencias").then().statusCode(200).body("codigo", hasItem(codigo));

        given().contentType(ContentType.JSON).body(Map.of("nome", "Centro Novo", "uf", "RJ", "ativa", false))
                .put("/agencias/" + codigo).then().statusCode(200)
                .body("nome", equalTo("Centro Novo")).body("ativa", equalTo(false));
        given().get("/agencias/" + codigo).then().body("uf", equalTo("RJ")).body("ativa", equalTo(false));

        given().delete("/agencias/" + codigo).then().statusCode(204);
        given().get("/agencias/" + codigo).then().statusCode(404)
                .body("type", equalTo("/problemas/nao-encontrado"))
                .body("detail", equalTo("Agência " + codigo + " não encontrada"));
        given().get("/agencias").then().body("codigo", not(hasItem(codigo)));
    }

    @Test
    void codigoDuplicado() {
        var codigo = codigo();
        criar(codigo);

        given().contentType(ContentType.JSON).body(Map.of("codigo", codigo, "nome", "Outra", "uf", "SP"))
                .post("/agencias").then().statusCode(422)
                .body("detail", equalTo("Já existe agência com o código " + codigo));
    }

    @Test
    void dadosInvalidos() {
        given().contentType(ContentType.JSON).body(Map.of("codigo", codigo(), "nome", "Centro", "uf", "São Paulo"))
                .post("/agencias").then().statusCode(422)
                .body("type", equalTo("/problemas/regra-violada"))
                .body("detail", equalTo("UF inválida: São Paulo"));
        given().contentType(ContentType.JSON).body(Map.of("nome", "Centro", "uf", "SP"))
                .put("/agencias/9999x").then().statusCode(422)
                .body("detail", equalTo("Código da agência deve ter 4 dígitos: 9999x"));
    }

    @Test
    void alterarOuExcluirInexistente() {
        given().contentType(ContentType.JSON).body(Map.of("nome", "Centro", "uf", "SP"))
                .put("/agencias/0001").then().statusCode(404);
        given().delete("/agencias/0001").then().statusCode(404);
    }
}
