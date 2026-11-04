package com.example.pagamento.entrypoint;

import com.example.pagamento.adapter.banco.BancoPagamentos;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ponta a ponta: HTTP -> use case -> domínio -> adapters -> H2.
 * Sistemas externos simulados: o EMISSOR decodifica a linha digitável (adapter fake);
 * o BANCO recebe os envios (adapter fake) e devolve o resultado pelo webhook.
 */
@QuarkusTest
class PagamentoResourceTest {

    private static final String API = "/api/v1/pagamentos";
    private static final String WEBHOOK_BANCO = "/webhooks/v1/banco/retornos";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger(1);

    @Inject
    BancoPagamentos banco;

    // ============================== apoio ==============================

    private static LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }

    private static LocalDate diaUtil() {
        var d = hoje().plusDays(5);
        return switch (d.getDayOfWeek()) {
            case SATURDAY -> d.plusDays(2);
            case SUNDAY -> d.plusDays(1);
            default -> d;
        };
    }

    /**
     * Linha no layout FAKE do emissor: 9 + emissor(3) + id(25) + vencimento + centavos(10).
     * Os 4 últimos dígitos do id identificam o beneficiário.
     */
    private static String linha(int fornecedor, long centavos) {
        var id = SEQUENCIA.incrementAndGet() * 10_000L + fornecedor;
        return "9001" + "%025d".formatted(id) + hoje().plusDays(30).toString().replace("-", "") + "%010d".formatted(centavos);
    }

    private static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    private static ValidatableResponse put(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().put(path).then();
    }

    private static String criar(String modalidade) {
        return post(API, "{\"modalidade\":\"" + modalidade + "\"}").statusCode(201).extract().path("id");
    }

    private static ValidatableResponse salvarBoletos(String id, String... linhas) {
        var lista = linhas.length == 0 ? "" : "\"" + String.join("\",\"", linhas) + "\"";
        return put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[" + lista + "]}");
    }

    private static void dadosGerais(String id, LocalDate data) {
        put(API + "/" + id, "{\"contaDebito\":{\"agencia\":\"1234\",\"conta\":\"998877\"},\"dataPagamento\":\"" + data + "\"}")
                .statusCode(200);
    }

    /** Cria, preenche, submete e envia ao banco. Devolve o id da transação. */
    private static String enviado(String id, String... linhas) {
        salvarBoletos(id, linhas).statusCode(200);
        dadosGerais(id, diaUtil());
        post(API + "/" + id + "/submissao", "").statusCode(200);
        return post(API + "/" + id + "/envio", "").statusCode(200).extract().path("transacao.id");
    }

    private static ValidatableResponse bancoRetorna(String idTransacao, String situacao, Long centavos) {
        var corpo = """
                {"idTransacao":"%s","situacao":"%s","valorLiquidadoCentavos":%s,"autenticacao":"AUT-%s",
                 "motivoRecusa":%s,"dataHora":"%sT10:15:00-03:00"}
                """.formatted(idTransacao, situacao, centavos, idTransacao,
                "RECUSADO".equals(situacao) ? "\"saldo insuficiente\"" : "null", hoje());
        return given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-banco-local").body(corpo)
                .when().post(WEBHOOK_BANCO).then();
    }

    // ============================== fluxo principal ==============================

    @Test
    void fluxoCompletoDoRascunhoAoPagamento() {
        // 1. rascunho incompleto: só a modalidade
        String id = post(API, "{\"modalidade\":\"PROCESSO\"}").statusCode(201)
                .header("Location", notNullValue()).body("status", equalTo("EM_EDICAO")).extract().path("id");
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("PAGAMENTO_SEM_BOLETOS"));

        // 2. seção de boletos: o usuário manda as linhas; os dados vêm do EMISSOR
        salvarBoletos(id, linha(7, 30000), linha(7, 20000)).statusCode(200)
                .body("boletos", hasSize(2)).body("valorTotal", equalTo(500.00f))
                .body("boletos[0].beneficiario", equalTo("Fornecedor 0007"));
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("CONTA_DEBITO_NAO_INFORMADA"));

        // 3. dados gerais e submissão
        dadosGerais(id, diaUtil());
        post(API + "/" + id + "/submissao", "").statusCode(200).body("status", equalTo("AGENDADO"));
        salvarBoletos(id).statusCode(409).body("codigo", equalTo("TRANSICAO_ESTADO_INVALIDA")); // não edita mais

        // 4. envio ao BANCO (saída) e retorno pelo webhook (entrada)
        String transacao = post(API + "/" + id + "/envio", "").statusCode(200)
                .body("status", equalTo("EM_PROCESSAMENTO")).extract().path("transacao.id");
        assertTrue(banco.envios().stream().anyMatch(e -> e.idTransacao().equals(transacao)));
        post(API + "/" + id + "/cancelamento", "{\"motivo\":\"desisti\"}").statusCode(409); // já no banco

        bancoRetorna(transacao, "LIQUIDADO", 50000L).statusCode(200).body("status", equalTo("PAGO"));
        bancoRetorna(transacao, "LIQUIDADO", 50000L).statusCode(200).body("status", equalTo("PAGO")); // idempotente
        given().get(API + "/" + id).then().body("transacao.autenticacao", equalTo("AUT-" + transacao))
                .body("analise", nullValue());
    }

    // ============================== regras por modalidade (cadeia) ==============================

    @Test
    void individualAceitaUmBoletoSo() {
        String id = criar("INDIVIDUAL");
        salvarBoletos(id, linha(1, 100), linha(1, 200)).statusCode(200); // a seção aceita; a submissão não
        dadosGerais(id, diaUtil());
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("QUANTIDADE_DE_BOLETOS_INVALIDA"));
    }

    @Test
    void processoExigeMesmoBeneficiario() {
        String id = criar("PROCESSO");
        salvarBoletos(id, linha(1, 100), linha(2, 200)).statusCode(200);
        dadosGerais(id, diaUtil());
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("BENEFICIARIOS_DIFERENTES"));
    }

    @Test
    void loteAceitaBeneficiariosDiferentesAteOLimite() {
        String id = criar("LOTE");
        salvarBoletos(id, linha(1, 60_000_000L), linha(2, 40_000_001L)).statusCode(200); // R$ 1.000.000,01
        dadosGerais(id, diaUtil());
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("VALOR_TOTAL_ACIMA_DO_LIMITE"));

        salvarBoletos(id, linha(1, 60_000_000L), linha(2, 40_000_000L)).statusCode(200);
        post(API + "/" + id + "/submissao", "").statusCode(200).body("status", equalTo("AGENDADO"));
    }

    // ============================== fatos do banco ==============================

    @Test
    void recusaDoBancoPermiteReenviarComNovaTransacao() {
        String id = criar("INDIVIDUAL");
        String primeira = enviado(id, linha(1, 10000));
        bancoRetorna(primeira, "RECUSADO", null).statusCode(200).body("status", equalTo("RECUSADO"));

        String segunda = post(API + "/" + id + "/reenvio", "").statusCode(200)
                .body("status", equalTo("EM_PROCESSAMENTO")).body("transacao.envios", equalTo(2))
                .extract().path("transacao.id");
        bancoRetorna(primeira, "RECUSADO", null).statusCode(404); // transação antiga não é mais a do pagamento
        bancoRetorna(segunda, "LIQUIDADO", 10000L).statusCode(200).body("status", equalTo("PAGO"));
    }

    @Test
    void liquidadoDivergenteVaiParaAnalise() {
        String id = criar("LOTE");
        String transacao = enviado(id, linha(1, 10000), linha(2, 5000));
        bancoRetorna(transacao, "LIQUIDADO", 14999L).statusCode(200).body("status", equalTo("EM_ANALISE"));
        given().get(API + "/" + id).then().body("analise.codigo", equalTo("VALOR_LIQUIDADO_DIVERGENTE"));
        post(API + "/" + id + "/cancelamento", "{\"motivo\":\"estorno combinado\"}").statusCode(200)
                .body("status", equalTo("CANCELADO"));
    }

    // ============================== CRUD e borda ==============================

    @Test
    void crudDoRascunho() {
        String id = criar("PROCESSO");
        given().queryParam("modalidade", "PROCESSO").queryParam("status", "EM_EDICAO").queryParam("tamanho", 100)
                .get(API).then().statusCode(200).body("itens.id", hasItem(id));
        given().delete(API + "/" + id).then().statusCode(204);
        given().get(API + "/" + id).then().statusCode(404).body("codigo", equalTo("PAGAMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void emissorRecusaBoletoIndisponivel() {
        String id = criar("INDIVIDUAL");
        salvarBoletos(id, "0".repeat(47)).statusCode(422).body("codigo", equalTo("BOLETO_INDISPONIVEL_NO_EMISSOR"));
    }

    @Test
    void dadosMalFormados() {
        post(API, "{\"modalidade\":\"XPTO\"}").statusCode(400).body("codigo", equalTo("REQUISICAO_INVALIDA"));
        post(API, "{}").statusCode(400).body("codigo", equalTo("REQUISICAO_INVALIDA"));
        String id = criar("INDIVIDUAL");
        salvarBoletos(id, "123").statusCode(400).body("codigo", equalTo("LINHA_DIGITAVEL_INVALIDA"));
        salvarBoletos(id, linha(1, 10000)).statusCode(200);
        dadosGerais(id, hoje().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("DATA_PAGAMENTO_EM_DIA_NAO_UTIL"));
    }

    @Test
    void webhookExigeTokenEConheceATransacao() {
        given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-errado")
                .body("{\"idTransacao\":\"x\",\"situacao\":\"LIQUIDADO\",\"valorLiquidadoCentavos\":1,\"dataHora\":\"2026-11-04T10:00:00-03:00\"}")
                .post(WEBHOOK_BANCO).then().statusCode(401).body("codigo", equalTo("NAO_AUTORIZADO"));
        bancoRetorna("transacao-inexistente", "LIQUIDADO", 1L).statusCode(404)
                .body("codigo", equalTo("PAGAMENTO_NAO_ENCONTRADO"));
    }
}
