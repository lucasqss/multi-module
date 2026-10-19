package com.example.processo;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ponta a ponta contra o jar: REST → motor → Oracle (PROCESSO + PROCESSO_IMOBILIARIO) → impressão, e a leitura
 * pelo lado query.
 * Os testes compartilham o banco, então cada um usa uma matrícula própria.
 */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
class CriarProcessoImobiliarioIT {

    /** Começa com 1: nunca cai no prefixo que faz o sistema de PDF falso falhar. */
    static String matricula() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
    }

    static ValidatableResponse criar(String cpf, String valor, String matricula, String tipo, String avaliacao) {
        var corpo = Map.of(
                "proponente", Map.of("nome", "Ana Souza", "cpf", cpf),
                "valor", valor,
                "imovel", Map.of("matricula", matricula, "tipo", tipo, "valorAvaliacao", avaliacao, "uf", "SP"));
        return given().contentType(ContentType.JSON).body(corpo).post("/processos/imobiliarios").then();
    }

    static ValidatableResponse criar(String matricula) {
        return criar("52998224725", "400000.00", matricula, "RESIDENCIAL", "500000.00");
    }

    static JsonPath pedidoDePdf(String numero) {
        return SistemaDePdfFalso.PEDIDOS.stream().map(JsonPath::from)
                .filter(pedido -> numero.equals(pedido.getString("referencia")))
                .findFirst().orElseThrow(() -> new AssertionError("Nenhum PDF pedido para " + numero));
    }

    @Test
    void criaProcessoEPedeOPdfDeAbertura() {
        var matricula = matricula();

        var resposta = criar(matricula).statusCode(201)
                .header("Location", matchesPattern(".*/processos/imobiliarios/[0-9a-f-]{36}"))
                .body("id", notNullValue())
                .body("numero", matchesPattern("IMO-" + LocalDate.now().getYear() + "-\\d{6}"))
                .extract();

        var pedido = pedidoDePdf(resposta.path("numero"));
        assertEquals("abertura-processo-imobiliario", pedido.getString("modelo"));
        assertEquals(resposta.<String>path("id"), pedido.getString("processo.id"));
        assertEquals("IMOBILIARIO", pedido.getString("processo.segmento"));
        assertEquals("ABERTO", pedido.getString("processo.situacao"));
        assertEquals("52998224725", pedido.getString("processo.proponente.cpf"));
        assertEquals(matricula, pedido.getString("processo.dados.matricula"));
        assertEquals("RESIDENCIAL", pedido.getString("processo.dados.tipoImovel"));
    }

    @Test
    void detalheTrazDadosDoPaiEDaFilha() {
        var matricula = matricula();
        var criado = criar(matricula).statusCode(201).extract();

        var detalhe = given().get(criado.header("Location")).then()
                .statusCode(200)
                .body("numero", equalTo(criado.path("numero")))
                .body("proponenteNome", equalTo("Ana Souza"))
                .body("proponenteCpf", equalTo("52998224725"))
                .body("situacao", equalTo("ABERTO"))
                .body("dataAbertura", equalTo(LocalDate.now().toString()))
                .body("matricula", equalTo(matricula))
                .body("tipoImovel", equalTo("RESIDENCIAL"))
                .body("uf", equalTo("SP"))
                .extract().jsonPath();
        // Comparação numérica: o NUMBER do Oracle não guarda zeros à direita e o JSON sai 400000, não 400000.00
        assertEquals(0, new BigDecimal("400000.00").compareTo(new BigDecimal(detalhe.getString("valor"))));
        assertEquals(0, new BigDecimal("500000.00").compareTo(new BigDecimal(detalhe.getString("valorAvaliacao"))));
        given().get("/processos/imobiliarios/00000000-0000-0000-0000-000000000000").then().statusCode(404);
    }

    @Test
    void listagemDeTodosOsSegmentosComFiltroPorSituacao() {
        String numero = criar(matricula()).statusCode(201).extract().path("numero");

        given().get("/processos").then().statusCode(200)
                .body("find { it.numero == '%s' }.segmento".formatted(numero), equalTo("IMOBILIARIO"))
                .body("find { it.numero == '%s' }.proponente".formatted(numero), equalTo("Ana Souza"));
        given().queryParam("situacao", "ABERTO").get("/processos").then().statusCode(200)
                .body("numero", hasItem(numero));
        given().queryParam("situacao", "CANCELADO").get("/processos").then().statusCode(200)
                .body("numero", not(hasItem(numero)));
        given().queryParam("situacao", "QUALQUER").get("/processos").then().statusCode(404);
    }

    @Test
    void segundoProcessoAbertoParaAMesmaMatriculaEhRecusado() {
        // A recusa depende do que foi gravado: a consulta na filha filtra pela situação, que está na tabela pai
        var matricula = matricula();
        criar(matricula).statusCode(201);

        criar(matricula).statusCode(422)
                .body("motivo", equalTo("Já existe processo aberto para a matrícula " + matricula + "/SP"));
    }

    @Test
    void numeracaoUnicaECrescente() {
        String primeiro = criar(matricula()).statusCode(201).extract().path("numero");
        String segundo = criar(matricula()).statusCode(201).extract().path("numero");

        assertTrue(segundo.compareTo(primeiro) > 0, primeiro + " deveria vir antes de " + segundo);
    }

    @Test
    void regrasDoMotorEDoSegmentoRespondem422() {
        criar("12345678900", "1000.00", matricula(), "RESIDENCIAL", "500000.00").statusCode(422)
                .body("motivo", equalTo("CPF inválido: 12345678900"));
        criar("52998224725", "300000.01", matricula(), "COMERCIAL", "500000.00").statusCode(422)
                .body("motivo", equalTo("Valor excede 60% da avaliação para imóvel comercial (máximo 300000.00)"));
    }

    @Test
    void falhaNoSistemaDePdfNaoDesfazOProcesso() {
        var matricula = SistemaDePdfFalso.FALHA + ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);
        String numero = criar(matricula).statusCode(201).extract().path("numero");

        pedidoDePdf(numero);                // o PDF foi pedido e o sistema respondeu 500...
        criar(matricula).statusCode(422);   // ...mas o processo continua gravado e aberto
    }
}
