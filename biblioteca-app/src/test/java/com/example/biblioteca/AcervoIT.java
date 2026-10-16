package com.example.biblioteca;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.example.biblioteca.Api.exemplar;
import static com.example.biblioteca.Api.livro;
import static com.example.biblioteca.Api.unico;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusIntegrationTest
class AcervoIT {

    @Test
    void livroCadastradoNoCommandApareceNoAcervoComReferenciaAbnt() {
        var titulo = "Dom Casmurro " + unico();
        var livro = livro(titulo, List.of("Machado de Assis"));
        exemplar(livro);
        exemplar(livro);

        given().queryParam("titulo", titulo.toLowerCase()).get("/acervo").then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].referencia", equalTo("ASSIS, Machado de. " + titulo + ". 2. ed. Rio de Janeiro: Garnier, 1899."))
                .body("[0].disponiveis", equalTo(2));
    }

    @Test
    void autoresMantemAOrdemDaObra() {
        var titulo = "Coletânea " + unico();
        livro(titulo, List.of("José de Alencar", "Machado de Assis", "João Silva Filho"));

        given().queryParam("titulo", titulo).get("/acervo").then()
                .body("[0].referencia", equalTo(
                        "ALENCAR, José de; ASSIS, Machado de; SILVA FILHO, João. " + titulo + ". 2. ed. Rio de Janeiro: Garnier, 1899."));
    }

    @Test
    void filtroPorAutorTrazTodosOsAutoresDoLivro() {
        var sobrenomeRaro = "Raro" + unico();
        var titulo = "Obra " + unico();
        livro(titulo, List.of("Machado de Assis", "Ana " + sobrenomeRaro));

        given().queryParam("autor", sobrenomeRaro.toUpperCase()).get("/acervo").then()
                .body("$", hasSize(1))
                .body("[0].referencia", equalTo("ASSIS, Machado de; " + sobrenomeRaro.toUpperCase() + ", Ana. "
                        + titulo + ". 2. ed. Rio de Janeiro: Garnier, 1899."));
    }

    @Test
    void exemplarBaixadoNaoContaComoDisponivel() {
        var titulo = "Baixas " + unico();
        var livro = livro(titulo, List.of("Graciliano Ramos"));
        exemplar(livro);
        given().delete("/exemplares/" + exemplar(livro)).then().statusCode(204);

        given().queryParam("titulo", titulo).get("/acervo").then().body("[0].disponiveis", equalTo(1));
    }

    @Test
    void isbnDuplicadoEhRecusado() {
        var isbn = "978" + unico();
        var corpo = Map.of("isbn", isbn, "titulo", "Vidas Secas", "autores", List.of("Graciliano Ramos"),
                "edicao", 1, "ano", 1938);
        Api.post("/livros", corpo).statusCode(201);
        Api.post("/livros", corpo).statusCode(422).body("motivo", equalTo("Já existe livro com ISBN " + isbn));
    }

    @Test
    void exemplarDeLivroInexistente() {
        Api.post("/livros/00000000-0000-0000-0000-000000000000/exemplares", Map.of("tombo", "T-" + unico()))
                .statusCode(404);
    }
}
