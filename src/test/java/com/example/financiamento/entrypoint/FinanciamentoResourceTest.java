package com.example.financiamento.entrypoint;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Ponta a ponta: HTTP -> use case -> domínio -> repositório -> Oracle (Dev Services).
 * Sistemas externos simulados: a AVALIAÇÃO devolve o bem a partir da matrícula/placa (adapter fake);
 * o ÓRGÃO DE REGISTRO recebe a garantia (adapter fake) e devolve o resultado pelo webhook.
 *
 * <p>CAIXA-PRETA de propósito (só HTTP, nada injetado): assim {@link FinanciamentoResourceIT} herda os mesmos
 * cenários e os roda contra a aplicação empacotada. O que precisa de CDI fica em {@code FinanciamentoRepositoryTest}.
 */
@QuarkusTest
class FinanciamentoResourceTest {

    static final String API = "/api/v1/financiamentos";
    static final String WEBHOOK_REGISTRO = "/webhooks/v1/registro/retornos";

    // ============================== apoio (também usado pelo IT) ==============================

    static LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }

    static ValidatableResponse post(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().post(path).then();
    }

    static ValidatableResponse put(String path, String body) {
        return given().contentType(ContentType.JSON).body(body).when().put(path).then();
    }

    static String criar(String tipo) {
        return post(API, "{\"tipo\":\"" + tipo + "\"}").statusCode(201).extract().path("id");
    }

    static ValidatableResponse dadosGerais(String id, String renda, String valor, int prazo) {
        return put(API + "/" + id, """
                {"proponente":{"nome":"Maria Silva","cpf":"123.456.789-01","rendaMensal":%s},
                 "valorSolicitado":%s,"prazoMeses":%d}""".formatted(renda, valor, prazo));
    }

    static ValidatableResponse garantia(String id, String identificacao) {
        return put(API + "/" + id + "/garantia", "{\"identificacao\":\"" + identificacao + "\"}");
    }

    static ValidatableResponse documentos(String id, String... tipos) {
        var lista = Arrays.stream(tipos).map(t -> "{\"tipo\":\"" + t + "\",\"referencia\":\"DOC-" + t + "\"}")
                .collect(Collectors.joining(","));
        return put(API + "/" + id + "/documentos", "{\"documentos\":[" + lista + "]}");
    }

    static ValidatableResponse submeter(String id) {
        return post(API + "/" + id + "/submissao", "");
    }

    /** Imóvel de R$ 500.000 (matrícula 500), completo e aprovado. */
    static String imovelAprovado() {
        String id = criar("IMOVEL");
        dadosGerais(id, "20000.00", "300000.00", 360).statusCode(200);
        garantia(id, "500").statusCode(200);
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "MATRICULA_IMOVEL").statusCode(200);
        submeter(id).statusCode(200);
        return id;
    }

    /** Veículo placa ABC1D23 (ano 2023, R$ 100.000), completo e aprovado. */
    static String veiculoAprovado() {
        String id = criar("VEICULO");
        dadosGerais(id, "10000.00", "60000.00", 48).statusCode(200);
        garantia(id, "ABC1D23").statusCode(200);
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "CRLV").statusCode(200);
        submeter(id).statusCode(200);
        return id;
    }

    /** Contrata e devolve o protocolo enviado ao órgão de registro. */
    static String contratar(String id) {
        return post(API + "/" + id + "/contratacao", "").statusCode(200).extract().path("registro.protocolo");
    }

    static ValidatableResponse orgaoRetorna(String protocolo, String situacao, String identificacaoRegistrada) {
        var corpo = """
                {"protocolo":"%s","situacao":"%s","identificacaoRegistrada":%s,"numeroRegistro":%s,
                 "motivoRecusa":%s,"dataHora":"%sT10:15:00-03:00"}
                """.formatted(protocolo, situacao,
                identificacaoRegistrada == null ? "null" : "\"" + identificacaoRegistrada + "\"",
                "REGISTRADO".equals(situacao) ? "\"REG-" + protocolo + "\"" : "null",
                "RECUSADO".equals(situacao) ? "\"documentação incompleta\"" : "null", hoje());
        return given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-registro-local").body(corpo)
                .when().post(WEBHOOK_REGISTRO).then();
    }

    // ============================== fluxo principal ==============================

    @Test
    void fluxoCompletoDoImovelDoRascunhoAoContrato() {
        // 1. rascunho incompleto: só o tipo
        String id = post(API, "{\"tipo\":\"IMOVEL\"}").statusCode(201)
                .header("Location", notNullValue()).body("status", equalTo("EM_EDICAO")).extract().path("id");
        submeter(id).statusCode(422).body("codigo", equalTo("PROPONENTE_NAO_INFORMADO"));

        // 2. dados gerais; a garantia vem do SISTEMA DE AVALIAÇÃO (o usuário só informa a matrícula)
        dadosGerais(id, "20000.00", "300000.00", 360).statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("GARANTIA_NAO_INFORMADA"));
        garantia(id, "500").statusCode(200)
                .body("garantia.valorAvaliado", equalTo(500000.00f)).body("garantia.anoModelo", nullValue());

        // 3. documentos: os comuns + os do tipo
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA").statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("DOCUMENTO_OBRIGATORIO_AUSENTE"));
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "MATRICULA_IMOVEL").statusCode(200).body("documentos", hasSize(3));
        submeter(id).statusCode(200).body("status", equalTo("APROVADO"));
        documentos(id).statusCode(409).body("codigo", equalTo("TRANSICAO_ESTADO_INVALIDA")); // não edita mais

        // 4. contratação: a garantia vai ao CARTÓRIO (saída) e o resultado volta pelo webhook (entrada)
        String protocolo = contratar(id);
        given().get(API + "/" + id).then().body("status", equalTo("EM_REGISTRO"));
        post(API + "/" + id + "/cancelamento", "{\"motivo\":\"desisti\"}").statusCode(409); // já no órgão

        orgaoRetorna(protocolo, "REGISTRADO", "500").statusCode(200).body("status", equalTo("CONTRATADO"));
        orgaoRetorna(protocolo, "REGISTRADO", "500").statusCode(200).body("status", equalTo("CONTRATADO")); // idempotente
        given().get(API + "/" + id).then().body("registro.numeroRegistro", equalTo("REG-" + protocolo))
                .body("pendencia", nullValue());
    }

    // ============================== regras por tipo (cadeia) ==============================

    @Test
    void imovelFinanciaAteOitentaPorCento() {
        String id = criar("IMOVEL");
        dadosGerais(id, "20000.00", "400000.01", 360).statusCode(200);
        garantia(id, "500").statusCode(200);
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "MATRICULA_IMOVEL").statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("VALOR_ACIMA_DO_PERCENTUAL"));

        dadosGerais(id, "20000.00", "400000.00", 360).statusCode(200);
        submeter(id).statusCode(200).body("status", equalTo("APROVADO"));
    }

    @Test
    void veiculoTemPrazoIdadeEDocumentoProprios() {
        String id = criar("VEICULO");
        dadosGerais(id, "10000.00", "60000.00", 72).statusCode(200);
        garantia(id, "ABC1D05").statusCode(200).body("garantia.anoModelo", equalTo(2005));
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "CRLV").statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("PRAZO_INVALIDO"));

        dadosGerais(id, "10000.00", "60000.00", 48).statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("VEICULO_MUITO_ANTIGO"));

        garantia(id, "ABC1D23").statusCode(200);
        submeter(id).statusCode(200).body("status", equalTo("APROVADO"));
    }

    @Test
    void parcelaNaoPassaDeTrintaPorCentoDaRenda() {
        String id = criar("VEICULO");
        dadosGerais(id, "1000.00", "60000.00", 48).statusCode(200); // parcela 1.250 > 300
        garantia(id, "ABC1D23").statusCode(200);
        documentos(id, "IDENTIDADE", "COMPROVANTE_RENDA", "CRLV").statusCode(200);
        submeter(id).statusCode(422).body("codigo", equalTo("PARCELA_ACIMA_DA_RENDA"));
    }

    // ============================== fatos do órgão de registro ==============================

    @Test
    void registroRecusadoPermiteReenviarComNovoProtocolo() {
        String id = veiculoAprovado();
        String primeiro = contratar(id);
        orgaoRetorna(primeiro, "RECUSADO", null).statusCode(200).body("status", equalTo("REGISTRO_RECUSADO"));

        String segundo = post(API + "/" + id + "/reenvio-registro", "").statusCode(200)
                .body("status", equalTo("EM_REGISTRO")).body("registro.envios", equalTo(2))
                .extract().path("registro.protocolo");
        orgaoRetorna(primeiro, "RECUSADO", null).statusCode(200)   // aviso atrasado do 1º envio: 200, ignorado
                .body("status", equalTo("EM_REGISTRO"));
        orgaoRetorna(segundo, "REGISTRADO", "ABC1D23").statusCode(200).body("status", equalTo("CONTRATADO"));
    }

    @Test
    void registroDeOutroBemVaiParaPendencia() {
        String id = imovelAprovado();
        String protocolo = contratar(id);
        orgaoRetorna(protocolo, "REGISTRADO", "501").statusCode(200).body("status", equalTo("EM_PENDENCIA"));
        given().get(API + "/" + id).then().body("pendencia.codigo", equalTo("IDENTIFICACAO_REGISTRADA_DIVERGENTE"));
        post(API + "/" + id + "/cancelamento", "{\"motivo\":\"registro a corrigir\"}").statusCode(200)
                .body("status", equalTo("CANCELADO"));
    }

    // ============================== CRUD e borda ==============================

    @Test
    void crudDoRascunho() {
        String id = criar("VEICULO");
        given().queryParam("tipo", "VEICULO").queryParam("status", "EM_EDICAO").queryParam("tamanho", 100)
                .get(API).then().statusCode(200).body("itens.id", hasItem(id));
        given().delete(API + "/" + id).then().statusCode(204);
        given().get(API + "/" + id).then().statusCode(404).body("codigo", equalTo("FINANCIAMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void avaliacaoValidaEConheceOBem() {
        String imovel = criar("IMOVEL");
        garantia(imovel, "0123").statusCode(422).body("codigo", equalTo("BEM_NAO_ENCONTRADO_NA_AVALIACAO"));
        garantia(imovel, "ABC1D23").statusCode(400).body("codigo", equalTo("IDENTIFICACAO_DO_BEM_INVALIDA"));
        String veiculo = criar("VEICULO");
        garantia(veiculo, "12345").statusCode(400).body("codigo", equalTo("IDENTIFICACAO_DO_BEM_INVALIDA"));
    }

    @Test
    void dadosMalFormados() {
        post(API, "{\"tipo\":\"XPTO\"}").statusCode(400).body("codigo", equalTo("REQUISICAO_INVALIDA"));
        post(API, "{}").statusCode(400).body("codigo", equalTo("REQUISICAO_INVALIDA"));
        String id = criar("IMOVEL");
        put(API + "/" + id, "{\"proponente\":{\"nome\":\"Maria\",\"cpf\":\"123\",\"rendaMensal\":1000}}")
                .statusCode(400).body("codigo", equalTo("CPF_INVALIDO"));
        documentos(id, "IDENTIDADE", "IDENTIDADE").statusCode(422).body("codigo", equalTo("DOCUMENTO_REPETIDO"));
    }

    @Test
    void webhookExigeTokenEUmProtocoloNosso() {
        given().contentType(ContentType.JSON).header("X-Webhook-Token", "token-errado")
                .body("{\"protocolo\":\"x\",\"situacao\":\"RECUSADO\",\"dataHora\":\"2026-11-04T10:00:00-03:00\"}")
                .post(WEBHOOK_REGISTRO).then().statusCode(401).body("codigo", equalTo("NAO_AUTORIZADO"));
        orgaoRetorna(UUID.randomUUID() + "-1", "RECUSADO", null).statusCode(404)
                .body("codigo", equalTo("FINANCIAMENTO_NAO_ENCONTRADO"));
        orgaoRetorna("protocolo-inexistente", "RECUSADO", null).statusCode(400)
                .body("codigo", equalTo("ID_PROTOCOLO_INVALIDO"));
    }
}
