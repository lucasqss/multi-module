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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ponta a ponta: HTTP -> use case -> domínio -> adapters -> H2.
 * Os sistemas externos são simulados: o EMISSOR decodifica a linha digitável (adapter fake) e avisa
 * cancelamentos pelo webhook; o BANCO recebe envios (adapter fake) e devolve o resultado pelo webhook.
 */
@QuarkusTest
class ProcessoPagamentoResourceTest {

    private static final String API = "/api/v1/processos-pagamento";
    private static final String WEBHOOK_BANCO = "/webhooks/v1/banco/retornos";
    private static final String WEBHOOK_EMISSOR = "/webhooks/v1/emissor/boletos-cancelados";
    private static final AtomicInteger SEQUENCIA = new AtomicInteger(1000);

    @Inject
    BancoPagamentos banco;

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

    /** Linha digitável no layout FAKE do emissor: 9 + emissor(3) + id(25) + vencimento + centavos(10). */
    private static String linha(int id, long centavos, LocalDate vencimento) {
        return "9001" + "%025d".formatted(id) + vencimento.toString().replace("-", "") + "%010d".formatted(centavos);
    }

    private static String linha(long centavos) {
        return linha(SEQUENCIA.incrementAndGet(), centavos, hoje().plusDays(30));
    }

    private static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    private static ValidatableResponse put(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().put(path).then();
    }

    private static String criar(String solicitante) {
        return post(API, "{\"solicitante\":\"" + solicitante + "\"}").statusCode(201).extract().path("id");
    }

    private static void preencher(String id, String... linhas) {
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[\"" + String.join("\",\"", linhas) + "\"]}").statusCode(200);
        put(API + "/" + id, "{\"contaDebito\":{\"agencia\":\"1234\",\"conta\":\"998877\"},\"dataPagamento\":\"" + diaUtil() + "\"}")
                .statusCode(200);
    }

    /** Cria, preenche, submete, aprova e envia ao banco. Devolve o id da transação enviada. */
    private String enviado(String id, String... linhas) {
        preencher(id, linhas);
        post(API + "/" + id + "/submissao", "").statusCode(200);
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"A1111111\"}").statusCode(200);
        return post(API + "/" + id + "/envio", "").statusCode(200).extract().path("tentativas[-1].idTransacao");
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

    private static ValidatableResponse emissorCancela(String idBoleto) {
        return given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-emissor-local")
                .body("{\"idBoleto\":\"" + idBoleto + "\",\"dataHoraCancelamento\":\"" + hoje() + "T09:00:00-03:00\"}")
                .when().post(WEBHOOK_EMISSOR).then();
    }

    // ============================== fluxo principal ==============================

    @Test
    void fluxoCompletoDoRascunhoAoPagamento() {
        // 1. rascunho incompleto
        String id = post(API, "{\"solicitante\":\"F1000001\"}").statusCode(201)
                .header("Location", notNullValue()).body("status", equalTo("EM_EDICAO")).extract().path("id");
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("PROCESSO_SEM_BOLETOS"));

        // 2. seção de boletos: o usuário manda as linhas; os dados vêm do EMISSOR
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[\"" + linha(30000) + "\",\"" + linha(20000) + "\"]}")
                .statusCode(200).body("boletos", hasSize(2)).body("valorTotal", equalTo(500.00f))
                .body("boletos[0].beneficiario", notNullValue());
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("CONTA_DEBITO_NAO_INFORMADA"));

        // 3. dados gerais e submissão (completude ok)
        put(API + "/" + id, "{\"contaDebito\":{\"agencia\":\"1234\",\"conta\":\"998877\"},\"dataPagamento\":\"" + diaUtil() + "\"}")
                .statusCode(200);
        post(API + "/" + id + "/submissao", "").statusCode(200).body("status", equalTo("AGUARDANDO_APROVACAO"));
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[]}").statusCode(409); // não edita mais

        // 4. aprovação (segregação de funções)
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"F1000001\"}").statusCode(422)
                .body("codigo", equalTo("APROVADOR_IGUAL_AO_SOLICITANTE"));
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"A1111111\"}").statusCode(200)
                .body("status", equalTo("APROVADO")).body("aprovacoes", hasSize(1));

        // 5. envio ao BANCO (saída) e retorno pelo webhook (entrada)
        String transacao = post(API + "/" + id + "/envio", "").statusCode(200)
                .body("status", equalTo("EM_PROCESSAMENTO")).body("tentativas[0].status", equalTo("ENVIADA"))
                .extract().path("tentativas[0].idTransacao");
        assertTrue(banco.envios().stream().anyMatch(e -> e.idTransacao().equals(transacao)));
        post(API + "/" + id + "/cancelamento", "{\"motivo\":\"desisti\"}").statusCode(409); // já no banco

        bancoRetorna(transacao, "LIQUIDADO", 50000L).statusCode(200).body("status", equalTo("PAGO"));
        bancoRetorna(transacao, "LIQUIDADO", 50000L).statusCode(200); // reenvio: idempotente
        given().get(API + "/" + id).then().body("tentativas[0].status", equalTo("LIQUIDADA"))
                .body("tentativas[0].codigoAutenticacao", equalTo("AUT-" + transacao)).body("analise", nullValue());
    }

    @Test
    void alcadaAcimaDeDezMilExigeDuasAprovacoes() {
        String id = criar("F1000002");
        preencher(id, linha(1_000_001));
        post(API + "/" + id + "/submissao", "").statusCode(200);
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"A1111111\"}").statusCode(200)
                .body("status", equalTo("AGUARDANDO_APROVACAO"));
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"A1111111\"}").statusCode(422)
                .body("codigo", equalTo("APROVADOR_REPETIDO"));
        post(API + "/" + id + "/aprovacoes", "{\"aprovador\":\"A2222222\"}").statusCode(200)
                .body("status", equalTo("APROVADO")).body("aprovacoes.aprovador", contains("A1111111", "A2222222"));
    }

    @Test
    void recusaDoBancoPermiteRetentar() {
        String id = criar("F1000003");
        String primeira = enviado(id, linha(10000));
        bancoRetorna(primeira, "RECUSADO", null).statusCode(200).body("status", equalTo("FALHOU"));

        String segunda = post(API + "/" + id + "/retentativa", "").statusCode(200)
                .body("tentativas", hasSize(2)).extract().path("tentativas[1].idTransacao");
        bancoRetorna(segunda, "LIQUIDADO", 10000L).statusCode(200).body("status", equalTo("PAGO"));
        given().get(API + "/" + id).then().body("tentativas[0].motivoRecusa", equalTo("saldo insuficiente"));
    }

    @Test
    void liquidadoDivergenteVaiParaAnalise() {
        String id = criar("F1000004");
        String transacao = enviado(id, linha(10000));
        bancoRetorna(transacao, "LIQUIDADO", 9900L).statusCode(200).body("status", equalTo("EM_ANALISE"));
        given().get(API + "/" + id).then().body("analise.codigo", equalTo("VALOR_LIQUIDADO_DIVERGENTE"));
    }

    @Test
    void rejeicaoEReabertura() {
        String id = criar("F1000005");
        preencher(id, linha(10000));
        post(API + "/" + id + "/submissao", "").statusCode(200);
        post(API + "/" + id + "/rejeicao", "{\"motivo\":\"conta errada\"}").statusCode(200)
                .body("status", equalTo("REJEITADO")).body("motivoRejeicao", equalTo("conta errada"));
        post(API + "/" + id + "/reabertura", "").statusCode(200).body("status", equalTo("EM_EDICAO"));
    }

    // ============================== fatos do EMISSOR ==============================

    @Test
    void boletoCanceladoNoEmissor_antesDoEnvioCancela_depoisVaiParaAnalise() {
        int idBoleto = SEQUENCIA.incrementAndGet();
        var mesmaLinha = linha(idBoleto, 10000, hoje().plusDays(30));

        String antes = criar("F1000006");
        preencher(antes, mesmaLinha);
        String depois = criar("F1000007");
        enviado(depois, mesmaLinha); // o mesmo boleto em dois processos

        emissorCancela("EMI-" + idBoleto).statusCode(200).body("processosAfetados", equalTo(2));

        given().get(API + "/" + antes).then().body("status", equalTo("CANCELADO"));
        given().get(API + "/" + depois).then().body("status", equalTo("EM_ANALISE"))
                .body("analise.codigo", equalTo("BOLETO_CANCELADO_NO_EMISSOR"));
        emissorCancela("EMI-" + idBoleto).statusCode(200); // aviso repetido: sem efeito
    }

    @Test
    void emissorRecusaBoletoIndisponivel() {
        String id = criar("F1000008");
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[\"" + "0".repeat(47) + "\"]}")
                .statusCode(422).body("codigo", equalTo("BOLETO_INDISPONIVEL_NO_EMISSOR"));
    }

    // ============================== borda / erros ==============================

    @Test
    void webhooksExigemOTokenDeCadaSistema() {
        given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-emissor-local")
                .body("{\"idTransacao\":\"x\",\"situacao\":\"LIQUIDADO\",\"valorLiquidadoCentavos\":1,\"dataHora\":\"2026-11-04T10:00:00-03:00\"}")
                .post(WEBHOOK_BANCO).then().statusCode(401).body("codigo", equalTo("NAO_AUTORIZADO"));
        bancoRetorna("transacao-inexistente", "LIQUIDADO", 1L).statusCode(404)
                .body("codigo", equalTo("PROCESSO_PAGAMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void crudDoRascunho() {
        String id = criar("F1000009");
        given().queryParam("solicitante", "f1000009").get(API).then().statusCode(200)
                .body("total", equalTo(1)).body("itens[0].id", equalTo(id));
        given().delete(API + "/" + id).then().statusCode(204);
        given().get(API + "/" + id).then().statusCode(404);
    }

    @Test
    void dadosMalFormados() {
        post(API, "{\"solicitante\":\"joao\"}").statusCode(400).body("codigo", equalTo("MATRICULA_INVALIDA"));
        String id = criar("F1000010");
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[\"123\"]}").statusCode(400)
                .body("codigo", equalTo("LINHA_DIGITAVEL_INVALIDA"));
        var sabado = hoje().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        put(API + "/" + id + "/boletos", "{\"linhasDigitaveis\":[\"" + linha(10000) + "\"]}").statusCode(200);
        put(API + "/" + id, "{\"contaDebito\":{\"agencia\":\"1234\",\"conta\":\"1\"},\"dataPagamento\":\"" + sabado + "\"}").statusCode(200);
        post(API + "/" + id + "/submissao", "").statusCode(422).body("codigo", equalTo("DATA_PAGAMENTO_EM_DIA_NAO_UTIL"));
    }
}
