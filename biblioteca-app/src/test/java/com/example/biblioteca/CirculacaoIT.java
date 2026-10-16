package com.example.biblioteca;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.example.biblioteca.Api.emprestar;
import static com.example.biblioteca.Api.emprestimo;
import static com.example.biblioteca.Api.exemplar;
import static com.example.biblioteca.Api.leitor;
import static com.example.biblioteca.Api.livro;
import static com.example.biblioteca.Api.unico;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusIntegrationTest
class CirculacaoIT {

    @Test
    void emprestimoApareceNoPainelComCitacaoEReduzDisponiveis() {
        var titulo = "Memórias Póstumas " + unico();
        var livro = livro(titulo, List.of("Machado de Assis"));
        var exemplar = exemplar(livro);
        var leitor = leitor();

        var emprestimo = emprestimo(leitor, exemplar);

        given().get("/leitores/" + leitor + "/painel").then()
                .statusCode(200)
                .body("emprestimos", hasSize(1))
                .body("emprestimos[0].emprestimoId", equalTo(emprestimo))
                .body("emprestimos[0].citacao", equalTo("(ASSIS, 1899)"))
                .body("emprestimos[0].dataPrevista", equalTo(LocalDate.now().plusDays(14).toString()))
                .body("emprestimos[0].atrasado", equalTo(false));
        given().queryParam("titulo", titulo).get("/acervo").then().body("[0].disponiveis", equalTo(0));
    }

    @Test
    void exemplarEmprestadoNaoPodeSerEmprestadoNemBaixado() {
        var exemplar = exemplar(livro("Iracema " + unico(), List.of("José de Alencar")));
        emprestimo(leitor(), exemplar);

        emprestar(leitor(), exemplar).statusCode(422);
        given().delete("/exemplares/" + exemplar).then().statusCode(422);
    }

    @Test
    void quartoEmprestimoSimultaneoEhRecusado() {
        var livro = livro("Senhora " + unico(), List.of("José de Alencar"));
        var leitor = leitor();
        for (int i = 0; i < 3; i++) {
            emprestimo(leitor, exemplar(livro));
        }

        emprestar(leitor, exemplar(livro)).statusCode(422)
                .body("motivo", equalTo("Limite de 3 empréstimos abertos atingido"));
    }

    @Test
    void renovarEstendePrazoAteODuasVezes() {
        var leitor = leitor();
        var emprestimo = emprestimo(leitor, exemplar(livro("Lucíola " + unico(), List.of("José de Alencar"))));

        Api.post("/emprestimos/" + emprestimo + "/renovacao").statusCode(200)
                .body("dataPrevista", equalTo(LocalDate.now().plusDays(14).toString()));
        Api.post("/emprestimos/" + emprestimo + "/renovacao").statusCode(200);
        Api.post("/emprestimos/" + emprestimo + "/renovacao").statusCode(422)
                .body("motivo", equalTo("Limite de 2 renovações atingido"));
        given().get("/leitores/" + leitor + "/painel").then().body("emprestimos[0].renovacoes", equalTo(2));
    }

    @Test
    void devolucaoNoPrazoLiberaExemplarSemSuspender() {
        var exemplar = exemplar(livro("Quincas Borba " + unico(), List.of("Machado de Assis")));
        var leitor = leitor();
        var emprestimo = emprestimo(leitor, exemplar);

        Api.post("/emprestimos/" + emprestimo + "/devolucao").statusCode(204);

        given().get("/leitores/" + leitor + "/painel").then()
                .body("emprestimos", hasSize(0))
                .body("suspensoAte", equalTo(null));
        emprestar(leitor(), exemplar).statusCode(201);
        Api.post("/emprestimos/" + emprestimo + "/devolucao").statusCode(422);
    }

    @Test
    void recursosInexistentes() {
        var inexistente = "00000000-0000-0000-0000-000000000000";
        given().get("/leitores/" + inexistente + "/painel").then().statusCode(404);
        emprestar(inexistente, exemplar(livro("Helena " + unico(), List.of("Machado de Assis")))).statusCode(404);
        Api.post("/emprestimos/" + inexistente + "/devolucao").statusCode(404);
    }
}
