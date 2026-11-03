package com.example.boleto.entrypoint;

import com.example.boleto.adapter.notificacao.NotificadorBoleto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ponta a ponta: HTTP -> use case -> domínio -> adapters -> H2.
 * Cada teste usa um pagador (documento) diferente para não interferir nos outros.
 */
@QuarkusTest
class BoletoResourceTest {

    private static final String PATH = "/api/v1/boletos";

    @Inject
    NotificadorBoleto notificador;

    private static LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }

    private static String diaUtil() {
        var data = hoje().plusDays(5);
        var util = switch (data.getDayOfWeek()) {
            case SATURDAY -> data.plusDays(2);
            case SUNDAY -> data.plusDays(1);
            default -> data;
        };
        return util.toString();
    }

    private static String corpo(String documento, String valor, String vencimento) {
        return """
                {"pagador":{"nome":"Maria Silva","documento":"%s","email":"maria@example.com"},
                 "valor":%s,"dataVencimento":"%s"}
                """.formatted(documento, valor, vencimento);
    }

    private static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    private static String emitir(String documento) {
        return post(PATH, corpo(documento, "500.00", diaUtil())).statusCode(201).extract().path("id");
    }

    private static String pagamento(String valor) {
        return "{\"valorPago\":%s,\"dataPagamento\":\"%s\"}".formatted(valor, hoje());
    }

    @Test
    void crudCompleto() {
        // CREATE
        String id = post(PATH, corpo("11111111111", "1500.00", diaUtil()))
                .statusCode(201)
                .header("Location", notNullValue())
                .body("status", equalTo("EMITIDO"))
                .body("linhaDigitavel", hasLength(47))
                .body("pagador.nome", equalTo("Maria Silva"))
                .extract().path("id");

        // READ
        given().get(PATH + "/" + id).then().statusCode(200).body("valor", equalTo(1500.00f));

        // LIST
        given().queryParam("documentoPagador", "11111111111")
                .get(PATH).then().statusCode(200)
                .body("total", equalTo(1))
                .body("totalPaginas", equalTo(1))
                .body("itens[0].id", equalTo(id));

        // UPDATE
        given().contentType(ContentType.JSON)
                .body("{\"valor\":800.00,\"dataVencimento\":\"" + diaUtil() + "\"}")
                .put(PATH + "/" + id)
                .then().statusCode(200)
                .body("valor", equalTo(800.00f))
                .body("linhaDigitavel", endsWith("0000080000"));

        // DELETE
        given().delete(PATH + "/" + id).then().statusCode(204);
        given().get(PATH + "/" + id).then().statusCode(404).body("codigo", equalTo("BOLETO_NAO_ENCONTRADO"));
    }

    @Test
    void cicloDeVidaEmitirNotificarPagar() {
        String id = emitir("22222222222");
        String boleto = PATH + "/" + id;

        // notificação de emissão
        post(boleto + "/notificacao", "")
                .statusCode(200).body("status", equalTo("NOTIFICADO")).body("notificadoEm", notNullValue());
        assertTrue(notificador.enviadas().stream().anyMatch(n -> n.destinatario().equals("maria@example.com")));

        // notificado: não altera nem exclui mais (409); reenviar notificação é permitido
        given().contentType(ContentType.JSON)
                .body("{\"valor\":10.00,\"dataVencimento\":\"" + diaUtil() + "\"}")
                .put(boleto).then().statusCode(409).body("codigo", equalTo("TRANSICAO_ESTADO_INVALIDA"));
        given().delete(boleto).then().statusCode(409);
        post(boleto + "/notificacao", "").statusCode(200).body("status", equalTo("NOTIFICADO"));

        // pagamento com valor errado: regra de negócio (422), boleto continua aberto
        post(boleto + "/pagamento", pagamento("499.99"))
                .statusCode(422).body("codigo", equalTo("VALOR_PAGO_DIVERGENTE"));

        // recebimento do pagamento
        post(boleto + "/pagamento", pagamento("500.00"))
                .statusCode(200)
                .body("status", equalTo("PAGO"))
                .body("pagamento.valorPago", equalTo(500.00f));

        // estado final
        post(boleto + "/pagamento", pagamento("500.00")).statusCode(409);
        post(boleto + "/cancelamento", "{\"motivo\":\"desistência\"}").statusCode(409);
    }

    @Test
    void cancelamento() {
        String boleto = PATH + "/" + emitir("33333333333");
        post(boleto + "/cancelamento", "{\"motivo\":\"\"}").statusCode(400);
        post(boleto + "/cancelamento", "{\"motivo\":\"emitido por engano\"}")
                .statusCode(200).body("status", equalTo("CANCELADO")).body("pagamento", nullValue());
        post(boleto + "/notificacao", "").statusCode(409);
    }

    @Test
    void limiteDeBoletosEmAbertoPorPagador() {
        String primeiro = emitir("44444444444");
        emitir("44444444444");
        emitir("44444444444");
        post(PATH, corpo("44444444444", "500.00", diaUtil()))
                .statusCode(422).body("codigo", equalTo("LIMITE_BOLETOS_EM_ABERTO_EXCEDIDO"));

        // pago não está mais "em aberto": libera uma vaga
        post(PATH + "/" + primeiro + "/pagamento", pagamento("500.00")).statusCode(200);
        emitir("44444444444");
    }

    @Test
    void regraDeNegocioRetorna422ComCodigoEspecifico() {
        post(PATH, corpo("55555555555", "10000.01", diaUtil()))
                .statusCode(422).body("codigo", equalTo("LIMITE_PESSOA_FISICA_EXCEDIDO"));
        var sabado = hoje().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString();
        post(PATH, corpo("55555555555", "100.00", sabado))
                .statusCode(422).body("codigo", equalTo("VENCIMENTO_EM_DIA_NAO_UTIL"));
    }

    // ===================== formato único de erro =====================

    @Test
    void dadoMalFormadoRetorna400() {
        post(PATH, corpo("123", "100.00", diaUtil()))
                .statusCode(400).body("codigo", equalTo("DOCUMENTO_INVALIDO"));
        post(PATH, corpo("66666666666", "10.001", diaUtil()))
                .statusCode(400).body("codigo", equalTo("VALOR_MONETARIO_INVALIDO"));
    }

    @Test
    void campoObrigatorioAusenteRetorna400ComOsCampos() {
        post(PATH, "{\"pagador\":{\"nome\":\"Maria\",\"documento\":\"77777777777\",\"email\":\"m@example.com\"}}")
                .statusCode(400)
                .body("codigo", equalTo("REQUISICAO_INVALIDA"))
                .body("campos.campo", contains("dataVencimento", "valor"));
    }

    @Test
    void jsonMalFormadoRetorna400() {
        post(PATH, "{ isso não é json").statusCode(400).body("codigo", equalTo("REQUISICAO_INVALIDA"));
    }

    @Test
    void idInvalidoRetorna404ComCorpoPadrao() {
        given().get(PATH + "/nao-e-uuid").then()
                .statusCode(404).body("codigo", equalTo("RECURSO_NAO_ENCONTRADO"));
    }
}
