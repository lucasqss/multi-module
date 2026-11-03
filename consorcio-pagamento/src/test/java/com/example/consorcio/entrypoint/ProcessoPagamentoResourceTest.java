package com.example.consorcio.entrypoint;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Ponta a ponta: HTTP -> use case -> domínio -> adapters -> H2.
 * Cada teste usa uma cota diferente da massa fake para não interferir nos outros.
 */
@QuarkusTest
class ProcessoPagamentoResourceTest {

    private static final String PATH = "/api/v1/processos-pagamento";

    private static String diaUtil() {
        var data = LocalDate.now().plusDays(5);
        var util = switch (data.getDayOfWeek()) {
            case SATURDAY -> data.plusDays(2);
            case SUNDAY -> data.plusDays(1);
            default -> data;
        };
        return util.toString();
    }

    private static String sabado() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString();
    }

    private static String corpo(int cota, String valor, String forma, String vencimento, String extra) {
        return """
                {"grupo":1234,"cota":%d,"valor":%s,"formaPagamento":"%s","dataVencimento":"%s"%s}
                """.formatted(cota, valor, forma, vencimento, extra);
    }

    private static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    private static String criar(int cota) {
        return post(PATH, corpo(cota, "500.00", "BOLETO", diaUtil(), ""))
                .statusCode(201).extract().path("id");
    }

    @Test
    void crudCompleto() {
        // CREATE
        String id = post(PATH, corpo(1, "1500.00", "DEBITO_EM_CONTA", diaUtil(),
                ",\"agenciaDebito\":\"1234\",\"contaDebito\":\"998877\""))
                .statusCode(201)
                .header("Location", notNullValue())
                .body("status", equalTo("ABERTO"))
                .extract().path("id");

        // READ
        given().get(PATH + "/" + id).then().statusCode(200).body("valor", equalTo(1500.00f));

        // LIST
        given().queryParam("grupo", 1234).queryParam("cota", 1)
                .get(PATH).then().statusCode(200)
                .body("total", equalTo(1))
                .body("itens[0].id", equalTo(id));

        // UPDATE
        given().contentType(ContentType.JSON)
                .body("{\"valor\":800.00,\"formaPagamento\":\"PIX\",\"dataVencimento\":\"" + diaUtil() + "\"}")
                .put(PATH + "/" + id)
                .then().statusCode(200)
                .body("valor", equalTo(800.00f))
                .body("formaPagamento", equalTo("PIX"))
                .body("agenciaDebito", equalTo(null));

        // DELETE
        given().delete(PATH + "/" + id).then().statusCode(204);
        given().get(PATH + "/" + id).then().statusCode(404);
    }

    @Test
    void maquinaDeEstados() {
        String id = criar(2);
        String processo = PATH + "/" + id;

        // não pode abrir outro para a mesma cota enquanto há um em andamento
        post(PATH, corpo(2, "100.00", "PIX", diaUtil(), ""))
                .statusCode(422).body("codigo", equalTo("COTA_COM_PROCESSO_EM_ANDAMENTO"));

        post(processo + "/envio", "").statusCode(200).body("status", equalTo("EM_PROCESSAMENTO"));

        // EM_PROCESSAMENTO: alterar e excluir são recusados pelo estado -> 409
        given().contentType(ContentType.JSON)
                .body("{\"valor\":10.00,\"formaPagamento\":\"PIX\",\"dataVencimento\":\"" + diaUtil() + "\"}")
                .put(processo).then().statusCode(409).body("codigo", equalTo("TRANSICAO_INVALIDA"));
        given().delete(processo).then().statusCode(409);

        post(processo + "/rejeicao", "{\"motivo\":\"saldo insuficiente\"}")
                .statusCode(200).body("status", equalTo("REJEITADO"))
                .body("motivoRejeicao", equalTo("saldo insuficiente"));
        post(processo + "/reabertura", "").statusCode(200).body("status", equalTo("ABERTO"));
        post(processo + "/envio", "").statusCode(200);
        post(processo + "/confirmacao", "").statusCode(200).body("status", equalTo("CONFIRMADO"));

        // estado final
        post(processo + "/cancelamento", "{\"motivo\":\"desistência\"}").statusCode(409);

        // confirmado não está "em andamento": a cota pode abrir um novo processo
        criar(2);
    }

    @Test
    void cancelamentoExigeMotivo() {
        String processo = PATH + "/" + criar(3);
        post(processo + "/cancelamento", "{\"motivo\":\"\"}").statusCode(400);
        post(processo + "/cancelamento", "{\"motivo\":\"cliente desistiu\"}")
                .statusCode(200).body("status", equalTo("CANCELADO"));
    }

    @Test
    void retornaTodasAsViolacoesDeUmaVez() {
        post(PATH, corpo(4, "6000.00", "PIX", sabado(), ",\"agenciaDebito\":\"1234\",\"contaDebito\":\"1\""))
                .statusCode(422)
                .body("codigo", equalTo("REGRAS_DE_NEGOCIO_VIOLADAS"))
                .body("violacoes", hasSize(3))
                .body("violacoes.codigo", containsInAnyOrder(
                        "LIMITE_PIX_EXCEDIDO", "VENCIMENTO_EM_DIA_NAO_UTIL", "CONTA_DEBITO_INDEVIDA"));
    }

    @Test
    void cotaCanceladaRetorna422() {
        post(PATH, corpo(91, "100.00", "PIX", diaUtil(), ""))
                .statusCode(422).body("codigo", equalTo("COTA_NAO_ACEITA_PAGAMENTO"));
    }

    @Test
    void cotaInexistenteRetorna404() {
        post(PATH, corpo(999, "100.00", "PIX", diaUtil(), "")).statusCode(404);
    }

    @Test
    void campoObrigatorioAusenteRetorna400() {
        post(PATH, "{\"grupo\":1234,\"cota\":5}").statusCode(400);
    }

    @Test
    void idInvalidoRetorna404() {
        given().get(PATH + "/nao-e-uuid").then().statusCode(404);
    }
}
