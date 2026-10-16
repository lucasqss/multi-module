package com.example.biblioteca;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * Cliente HTTP dos testes de API: a massa de dados entra pelo lado command e é verificada pelo lado query.
 * Os testes compartilham o mesmo banco, então cada dado gerado aqui é único (ISBN, tombo, e-mail, título).
 */
final class Api {

    private Api() {
    }

    static String unico() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
    }

    static ValidatableResponse post(String caminho, Object corpo) {
        return given().contentType(ContentType.JSON).body(corpo).post(caminho).then();
    }

    static ValidatableResponse post(String caminho) {
        return given().post(caminho).then();
    }

    static String livro(String titulo, List<String> autores) {
        return post("/livros", Map.of("isbn", "978" + unico(), "titulo", titulo, "autores", autores,
                "edicao", 2, "local", "Rio de Janeiro", "editora", "Garnier", "ano", 1899))
                .statusCode(201).extract().path("id");
    }

    static String exemplar(String livroId) {
        return post("/livros/" + livroId + "/exemplares", Map.of("tombo", "T-" + unico()))
                .statusCode(201).extract().path("id");
    }

    static String leitor() {
        return post("/leitores", Map.of("nome", "Ana Souza", "email", "ana." + unico() + "@example.com"))
                .statusCode(201).extract().path("id");
    }

    static ValidatableResponse emprestar(String leitorId, String exemplarId) {
        return post("/emprestimos", Map.of("leitorId", leitorId, "exemplarId", exemplarId));
    }

    static String emprestimo(String leitorId, String exemplarId) {
        return emprestar(leitorId, exemplarId).statusCode(201).extract().path("id");
    }
}
