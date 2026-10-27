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
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ponta a ponta contra o jar: REST → motor → Oracle (PROCESSO + PROCESSO_IMOBILIARIO) → sistema de PDF, e a leitura
 * pelo lado query.
 * Os testes compartilham o banco, então cada um usa uma matrícula própria.
 */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
@WithTestResource(CadastroFalso.class)
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
        assertEquals("IMOBILIARIO", pedido.getString("segmento"));
        assertEquals("Ana Souza", pedido.getString("proponente"));
        assertEquals("52998224725", pedido.getString("cpf"));
        assertEquals(matricula, pedido.getString("campos.matricula"));
        assertEquals("RESIDENCIAL", pedido.getString("campos.tipoImovel"));
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

        var item = Listagem.porNumero(Listagem.todos(null), numero);
        assertEquals("IMOBILIARIO", item.get("segmento"));
        assertEquals("Ana Souza", item.get("proponente"));
        Listagem.porNumero(Listagem.todos("ABERTO"), numero);
        assertTrue(Listagem.todos("CANCELADO").stream().noneMatch(i -> numero.equals(i.get("numero"))));
        given().queryParam("situacao", "QUALQUER").get("/processos").then().statusCode(404);
    }

    @Test
    void listagemPaginada() {
        criar(matricula()).statusCode(201);
        criar(matricula()).statusCode(201);

        var primeira = given().queryParam("tamanho", 1).get("/processos").then().statusCode(200)
                .body("pagina", equalTo(1))
                .body("tamanho", equalTo(1))
                .body("itens.size()", equalTo(1))
                .extract().jsonPath();
        assertTrue(primeira.getLong("total") >= 2);
        given().queryParam("pagina", 2).queryParam("tamanho", 1).get("/processos").then().statusCode(200)
                .body("itens[0].numero", not(equalTo(primeira.getString("itens[0].numero"))));
    }

    @Test
    void errosNoFormatoProblemDetails() {
        criar("12345678900", "1000.00", matricula(), "RESIDENCIAL", "500000.00").statusCode(422)
                .contentType("application/problem+json")
                .body("type", equalTo("/problemas/regra-violada"))
                .body("title", equalTo("Regra de negócio violada"))
                .body("status", equalTo(422))
                .body("detail", equalTo("CPF inválido: 12345678900"));
        given().get("/processos/imobiliarios/00000000-0000-0000-0000-000000000000").then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problemas/nao-encontrado"))
                .body("status", equalTo(404));
    }

    @Test
    void paginacaoForaDosLimites() {
        given().queryParam("tamanho", 0).get("/processos").then().statusCode(400)
                .body("detail", equalTo("pagina deve ser >= 1 e tamanho entre 1 e 100"));
        given().queryParam("tamanho", 101).get("/processos").then().statusCode(400);
        given().queryParam("pagina", 0).get("/processos").then().statusCode(400);
    }

    @Test
    void segundoProcessoAbertoParaAMesmaMatriculaEhRecusado() {
        // A recusa depende do que foi gravado: a consulta na filha filtra pela situação, que está na tabela pai
        var matricula = matricula();
        criar(matricula).statusCode(201);

        criar(matricula).statusCode(422)
                .body("detail", equalTo("Já existe processo em andamento para a matrícula " + matricula + "/SP"));
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
                .body("detail", equalTo("CPF inválido: 12345678900"));
        criar("52998224725", "300000.01", matricula(), "COMERCIAL", "500000.00").statusCode(422)
                .body("detail", equalTo("Valor excede 60% da avaliação para imóvel comercial (máximo 300000.00)"));
    }

    @Test
    void situacaoCadastralIrregularNoSistemaExternoRecusa() {
        criar(CadastroFalso.CPF_IRREGULAR, "1000.00", matricula(), "RESIDENCIAL", "500000.00").statusCode(422)
                .body("detail", equalTo("Situação cadastral irregular para o CPF " + CadastroFalso.CPF_IRREGULAR));
    }

    @Test
    void sistemaDeCadastroForaDoArResponde503() {
        criar(CadastroFalso.CPF_SISTEMA_FORA, "1000.00", matricula(), "RESIDENCIAL", "500000.00").statusCode(503)
                .body("detail", equalTo("Sistema de cadastro indisponível; tente novamente mais tarde"));
    }

    @Test
    void falhaNoSistemaDePdfNaoDesfazOProcesso() {
        var matricula = SistemaDePdfFalso.FALHA + ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);
        String numero = criar(matricula).statusCode(201).extract().path("numero");

        pedidoDePdf(numero);                // o PDF foi pedido e o sistema respondeu 500...
        criar(matricula).statusCode(422);   // ...mas o processo continua gravado e aberto
    }
}
