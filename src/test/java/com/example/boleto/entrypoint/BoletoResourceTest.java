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
import static org.hamcrest.Matchers.hasSize;
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

    private static String rascunho(String documento, String vencimento) {
        return """
                {"pagador":{"nome":"Maria Silva","documento":"%s","email":"maria@example.com"},
                 "dataVencimento":"%s"}
                """.formatted(documento, vencimento);
    }

    private static String itens(String... valores) {
        var json = new StringBuilder("{\"itens\":[");
        for (int i = 0; i < valores.length; i++) {
            json.append(i == 0 ? "" : ",").append("{\"descricao\":\"Item %d\",\"valor\":%s}".formatted(i, valores[i]));
        }
        return json.append("]}").toString();
    }

    private static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    private static ValidatableResponse put(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().put(path).then();
    }

    private static String criar(String documento) {
        return post(PATH, rascunho(documento, diaUtil())).statusCode(201).extract().path("id");
    }

    /** Cria, preenche os itens e emite. */
    private static String emitido(String documento, String... valores) {
        String id = criar(documento);
        put(PATH + "/" + id + "/itens", itens(valores)).statusCode(200);
        post(PATH + "/" + id + "/emissao", "").statusCode(200);
        return id;
    }

    private static String pagamento(String valor) {
        return "{\"valorPago\":%s,\"dataPagamento\":\"%s\"}".formatted(valor, hoje());
    }

    @Test
    void edicaoPorPartesAteAEmissao() {
        // 1. cria o rascunho: incompleto, sem linha digitável
        String id = post(PATH, rascunho("11111111111", diaUtil()))
                .statusCode(201)
                .header("Location", notNullValue())
                .body("status", equalTo("EM_EDICAO"))
                .body("itens", hasSize(0))
                .body("linhaDigitavel", nullValue())
                .extract().path("id");
        String boleto = PATH + "/" + id;

        // 2. emitir agora falha por COMPLETUDE (422), e o boleto continua editável
        post(boleto + "/emissao", "").statusCode(422).body("codigo", equalTo("BOLETO_SEM_ITENS"));

        // 3. cada seção é salva por um caso de uso próprio, em qualquer ordem
        put(boleto + "/itens", itens("300.00", "200.00"))
                .statusCode(200).body("valor", equalTo(500.00f)).body("itens", hasSize(2));
        put(boleto + "/destinatarios", "{\"emails\":[\"financeiro@empresa.com\"]}")
                .statusCode(200).body("destinatarios", contains("financeiro@empresa.com"));
        put(boleto, "{\"dataVencimento\":\"" + diaUtil() + "\"}").statusCode(200);

        // 4. regra de SEÇÃO (422) não depende de o boleto estar completo
        put(boleto + "/itens", itens("100.00", "0")).statusCode(422).body("codigo", equalTo("ITEM_COM_VALOR_ZERADO"));

        // 5. emissão: completo -> EMITIDO com linha digitável do valor total
        post(boleto + "/emissao", "")
                .statusCode(200)
                .body("status", equalTo("EMITIDO"))
                .body("linhaDigitavel", hasLength(47))
                .body("linhaDigitavel", endsWith("0000050000"))
                .body("emitidoEm", notNullValue());

        // 6. emitido: seções, vencimento e exclusão são recusados pelo estado (409)
        put(boleto + "/itens", itens("10.00")).statusCode(409).body("codigo", equalTo("TRANSICAO_ESTADO_INVALIDA"));
        put(boleto, "{\"dataVencimento\":\"" + diaUtil() + "\"}").statusCode(409);
        given().delete(boleto).then().statusCode(409);
    }

    @Test
    void cicloDeVidaEmitirNotificarPagar() {
        String boleto = PATH + "/" + emitido("22222222222", "500.00");
        // notificação de emissão: pagador + destinatários extras
        post(boleto + "/notificacao", "")
                .statusCode(200).body("status", equalTo("NOTIFICADO")).body("notificadoEm", notNullValue());
        assertTrue(notificador.enviadas().stream().anyMatch(n -> n.destinatario().equals("maria@example.com")));
        post(boleto + "/notificacao", "").statusCode(200).body("status", equalTo("NOTIFICADO")); // reenvio

        post(boleto + "/pagamento", pagamento("499.99")).statusCode(422).body("codigo", equalTo("VALOR_PAGO_DIVERGENTE"));
        post(boleto + "/pagamento", pagamento("500.00"))
                .statusCode(200).body("status", equalTo("PAGO")).body("pagamento.valorPago", equalTo(500.00f));

        post(boleto + "/pagamento", pagamento("500.00")).statusCode(409);
        post(boleto + "/cancelamento", "{\"motivo\":\"desistência\"}").statusCode(409);
    }

    @Test
    void notificacaoVaiTambemParaOsDestinatariosExtras() {
        String id = criar("23232323232");
        put(PATH + "/" + id + "/itens", itens("50.00")).statusCode(200);
        put(PATH + "/" + id + "/destinatarios", "{\"emails\":[\"extra@empresa.com\"]}").statusCode(200);
        post(PATH + "/" + id + "/emissao", "").statusCode(200);
        post(PATH + "/" + id + "/notificacao", "").statusCode(200);
        assertTrue(notificador.enviadas().stream().anyMatch(n -> n.destinatario().equals("extra@empresa.com")));
    }

    @Test
    void crudDoRascunho() {
        String id = criar("33333333333");
        given().get(PATH + "/" + id).then().statusCode(200).body("status", equalTo("EM_EDICAO"));
        given().queryParam("documentoPagador", "33333333333").get(PATH).then().statusCode(200)
                .body("total", equalTo(1)).body("totalPaginas", equalTo(1)).body("itens[0].id", equalTo(id));
        given().delete(PATH + "/" + id).then().statusCode(204);
        given().get(PATH + "/" + id).then().statusCode(404).body("codigo", equalTo("BOLETO_NAO_ENCONTRADO"));
    }

    @Test
    void cancelamentoDeBoletoEmitido() {
        String boleto = PATH + "/" + emitido("34343434343", "100.00");
        post(boleto + "/cancelamento", "{\"motivo\":\"\"}").statusCode(400);
        post(boleto + "/cancelamento", "{\"motivo\":\"emitido por engano\"}")
                .statusCode(200).body("status", equalTo("CANCELADO"));
        post(boleto + "/notificacao", "").statusCode(409);
    }

    @Test
    void limiteDeBoletosEmAbertoContaSoOsEmitidos() {
        String primeiro = emitido("44444444444", "500.00");
        emitido("44444444444", "500.00");
        emitido("44444444444", "500.00");
        String quarto = criar("44444444444"); // rascunho não conta no limite
        put(PATH + "/" + quarto + "/itens", itens("500.00")).statusCode(200);
        post(PATH + "/" + quarto + "/emissao", "")
                .statusCode(422).body("codigo", equalTo("LIMITE_BOLETOS_EM_ABERTO_EXCEDIDO"));

        post(PATH + "/" + primeiro + "/pagamento", pagamento("500.00")).statusCode(200); // libera uma vaga
        post(PATH + "/" + quarto + "/emissao", "").statusCode(200);
    }

    @Test
    void regrasDeEmissaoRetornam422ComCodigoEspecifico() {
        String id = criar("55555555555");
        put(PATH + "/" + id + "/itens", itens("6000.00", "4000.01")).statusCode(200);
        post(PATH + "/" + id + "/emissao", "").statusCode(422).body("codigo", equalTo("LIMITE_PESSOA_FISICA_EXCEDIDO"));

        var sabado = hoje().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString();
        String outro = post(PATH, rascunho("55555555555", sabado)).statusCode(201).extract().path("id");
        put(PATH + "/" + outro + "/itens", itens("100.00")).statusCode(200);
        post(PATH + "/" + outro + "/emissao", "").statusCode(422).body("codigo", equalTo("VENCIMENTO_EM_DIA_NAO_UTIL"));
    }

    @Test
    void regraDeSecaoDeDestinatarios() {
        String boleto = PATH + "/" + criar("56565656565");
        put(boleto + "/destinatarios", "{\"emails\":[\"maria@example.com\"]}")
                .statusCode(422).body("codigo", equalTo("DESTINATARIO_REPETIDO"));
    }

    // ===================== formato único de erro =====================

    @Test
    void dadoMalFormadoRetorna400() {
        post(PATH, rascunho("123", diaUtil())).statusCode(400).body("codigo", equalTo("DOCUMENTO_INVALIDO"));
        String boleto = PATH + "/" + criar("66666666666");
        put(boleto + "/itens", itens("10.001")).statusCode(400).body("codigo", equalTo("VALOR_MONETARIO_INVALIDO"));
        put(boleto + "/destinatarios", "{\"emails\":[\"nao-e-email\"]}").statusCode(400).body("codigo", equalTo("EMAIL_INVALIDO"));
    }

    @Test
    void campoObrigatorioAusenteRetorna400ComOsCampos() {
        post(PATH, "{\"pagador\":{\"nome\":\"Maria\",\"documento\":\"77777777777\",\"email\":\"m@example.com\"}}")
                .statusCode(400)
                .body("codigo", equalTo("REQUISICAO_INVALIDA"))
                .body("campos.campo", contains("dataVencimento"));
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
