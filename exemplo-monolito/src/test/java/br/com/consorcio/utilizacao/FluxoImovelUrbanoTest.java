package br.com.consorcio.utilizacao;

import br.com.consorcio.utilizacao.integracao.outbox.ProcessadorOutbox;
import br.com.consorcio.utilizacao.suporte.AmbienteExterno;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Etapa 1 do roadmap de ponta a ponta, pelos canais REST, com Oracle real (Testcontainers)
 * e pagamentos/GED simulados (WireMock).
 */
@QuarkusTest
@WithTestResource(AmbienteExterno.class)
class FluxoImovelUrbanoTest {

    static final String CPF_TITULAR = "11122233344";

    @Inject
    ProcessadorOutbox outbox;

    @Test
    void fluxoCompletoDoImovelUrbanoPronto() throws Exception {
        // ---- F0/F1: abertura, participantes, reserva da cota
        long processo = abrirProcesso(new BigDecimal("280000.00"));
        long objeto = get("/backoffice/processos/" + processo).getLong("objetos[0].id");
        assertEstado(processo, "RASCUNHO");

        long vendedor = post("/backoffice/abertura/" + processo + "/participantes",
                Map.of("tipo", "PF", "cpfCnpj", "55566677788", "nome", "Vendedor Exemplo", "papel", "VENDEDOR"), "ana").getLong("id");
        long conta = post("/backoffice/abertura/" + processo + "/participantes/" + vendedor + "/contas",
                Map.of("banco", "001", "agencia", "1234", "conta", "99999-0", "titularidadeVerificada", true), "ana").getLong("id");

        long vinculo = post("/backoffice/abertura/" + processo + "/cotas",
                Map.of("cotaRef", "1001-0042", "valor", 280000), "ana").getLong("id");

        // A mesma cota não pode ficar reservada em dois processos.
        long outroProcesso = abrirProcesso(new BigDecimal("150000.00"));
        falha("/backoffice/abertura/" + outroProcesso + "/cotas", Map.of("cotaRef", "1001-0042", "valor", 100000), "ana", "MOT-015");

        // Fases respeitam dependências.
        falha("/backoffice/processos/" + processo + "/etapas/DOCUMENTOS/conclusao", Map.of(), "ana", "FASE_FORA_DE_ORDEM");

        post("/backoffice/abertura/" + processo + "/comunicacao-formal", Map.of(), "ana");
        assertEstado(processo, "ABERTO");

        // ---- F2: documento vai para o GED; guardamos só o id
        post("/backoffice/analise/" + processo + "/documentos", Map.of(
                "tipoDocumento", "MATRICULA_ATUALIZADA", "nomeArquivo", "matricula.pdf", "mime", "application/pdf",
                "conteudoBase64", Base64.getEncoder().encodeToString("%PDF-exemplo".getBytes()), "objetoId", objeto), "ana");
        AmbienteExterno.wiremock().verify(1, postRequestedFor(urlEqualTo("/ged/documentos")));

        // ---- F2 a F7: análises em paralelo, depois a decisão
        concluir(processo, "DOCUMENTOS");
        assertEstado(processo, "EM_ANALISE");
        concluir(processo, "QUALIFICACAO_BENEFICIARIO");
        concluir(processo, "CAPACIDADE");
        concluir(processo, "ANALISE_JURIDICA");
        falha("/backoffice/processos/" + processo + "/etapas/VISTORIA_AVALIACAO/conclusao", Map.of(), "ana", "LAUDO_PENDENTE");
        post("/backoffice/analise/" + processo + "/objetos/" + objeto + "/avaliacao", Map.of("valorAvaliado", 285000), "eng");
        concluir(processo, "VISTORIA_AVALIACAO");
        concluir(processo, "DECISAO_COMPLEMENTO");
        assertEstado(processo, "APROVADO");

        // ---- F8 a F12: seguro, instrumento com AF (cria a garantia), assinaturas, ITBI, registro
        concluir(processo, "SEGUROS");
        post("/backoffice/formalizacao/" + processo + "/instrumento", Map.of(), "juridico");
        assertEstado(processo, "EM_FORMALIZACAO");
        given().get("/backoffice/formalizacao/" + processo).then().statusCode(200)
                .body("instrumento.tipo", notNullValue())
                .body("garantias[0].situacao", is("PROPOSTA"))
                .body("fasesPendentes", hasItem("ASSINATURAS"));
        long garantia = get("/backoffice/processos/" + processo).getLong("garantias[0].id");
        concluir(processo, "ASSINATURAS");
        assertEstado(processo, "EM_CONSTITUICAO_GARANTIA");
        concluir(processo, "TRIBUTO_TRANSMISSAO");
        concluir(processo, "CONSTITUICAO_GARANTIA");

        // ---- F13: sem AF registrada e conferida, não fica apto a pagamento
        falha("/backoffice/processos/" + processo + "/etapas/CONFERENCIA_GARANTIA/conclusao", Map.of(), "ana", "GARANTIA_INSUFICIENTE");
        post("/backoffice/formalizacao/garantias/" + garantia + "/protocolo", Map.of("orgao", "1º RI de São Paulo", "protocolo", "P-123"), "juridico");
        post("/backoffice/formalizacao/garantias/" + garantia + "/registro", Map.of("numeroRegistro", "R-7/12345", "registradoEm", "2026-10-20"), "juridico");
        post("/backoffice/formalizacao/garantias/" + garantia + "/conferencia", Map.of(), "juridico");
        concluir(processo, "CONFERENCIA_GARANTIA");
        assertEstado(processo, "APTO_PAGAMENTO");

        // ---- F14: ordem com origem, quatro olhos, envio pela outbox
        long ordem = post("/backoffice/pagamento/ordens", mapa("processoId", processo,
                "beneficiarioParticipacaoId", vendedor, "contaRecebimentoId", conta, "finalidade", "PRECO_OBJETO", "meio", "TED",
                "origens", List.of(Map.of("processoCotaId", vinculo, "valor", 280000))), "ana").getLong("id");
        falha("/backoffice/pagamento/ordens/" + ordem + "/aprovacao", Map.of(), "ana", "QUATRO_OLHOS");
        post("/backoffice/pagamento/ordens/" + ordem + "/aprovacao", Map.of(), "bruno");
        assertEstado(processo, "EM_PAGAMENTO");

        // Sistema de pagamentos fora do ar: a mensagem fica na outbox e a ordem continua APROVADA.
        AmbienteExterno.wiremock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/pagamentos/ordens"))
                .willReturn(serviceUnavailable()));
        assertEquals(0, outbox.processarPendentes());
        given().get("/backoffice/pagamento/ordens/" + ordem).then().body("situacao", is("APROVADA"));

        // Volta ao ar: a repetição sai com a mesma chave de idempotência.
        AmbienteExterno.stubsPadrao();
        assertEquals(1, outbox.processarPendentes());
        AmbienteExterno.wiremock().verify(postRequestedFor(urlEqualTo("/pagamentos/ordens"))
                .withHeader("Idempotency-Key", equalTo("ORDEM-" + ordem)));
        given().get("/backoffice/pagamento/ordens/" + ordem).then().body("situacao", is("ENVIADA")).body("idExterno", is("PAG-0001"));

        // Retorno do sistema de pagamentos, repetido: o segundo é ignorado.
        Map<String, Object> retorno = Map.of("chaveIdempotencia", "RET-1", "ordemId", ordem, "resultado", "LIQUIDADO");
        post("/parceiro/pagamentos/retornos", retorno, "pagamentos");
        given().contentType(ContentType.JSON).body(retorno).post("/parceiro/pagamentos/retornos")
                .then().statusCode(200).body("situacao", is("LIQUIDADA"));
        assertEstado(processo, "PAGO");
        get("/backoffice/processos/" + processo).getList("cotas.situacao").forEach(s -> assertEquals("UTILIZADA", s));

        // Liquidação deixou o "informar utilização" na outbox; a procedure do core baixa o saldo.
        assertEquals(1, outbox.processarPendentes());
        assertEquals(new BigDecimal("20000.00"), saldoNoCore("1001-0042"));

        // ---- F15/F16: posse e encerramento
        concluir(processo, "ENTREGA");
        concluir(processo, "SOBRA_ENCERRAMENTO");
        assertEstado(processo, "CONCLUIDO");

        // Canal do consorciado enxerga o próprio processo, com menos detalhe.
        given().header("X-Cpf", CPF_TITULAR).get("/consorciado/processos")
                .then().statusCode(200)
                .body("find { it.id == " + processo + " }.estado", is("CONCLUIDO"))
                .body("find { it.id == " + processo + " }.fasesConcluidas", is(17));
    }

    @Test
    void recusaCotaNaoContempladaOuDeOutraCategoria() {
        long processo = abrirProcesso(new BigDecimal("100000.00"));
        falha("/backoffice/abertura/" + processo + "/cotas", Map.of("cotaRef", "1001-0043", "valor", 100000), "ana", "COTA_NAO_CONTEMPLADA");
        falha("/backoffice/abertura/" + processo + "/cotas", Map.of("cotaRef", "2001-0007", "valor", 30000), "ana", "MOT-011");
    }

    @Test
    void fiancaDaContemplacaoEhIdempotente() {
        Map<String, Object> fianca = Map.of("cotaRef", "2001-0007", "cpfCnpjFiador", "99988877766", "nomeFiador", "Fiador",
                "valorCobertura", 40000);
        long primeira = given().contentType(ContentType.JSON).header("Idempotency-Key", "CONT-77").header("X-Usuario", "contemplacao")
                .body(fianca).post("/parceiro/contemplacao/garantias/fiancas").then().statusCode(200).extract().jsonPath().getLong("id");
        long segunda = given().contentType(ContentType.JSON).header("Idempotency-Key", "CONT-77").header("X-Usuario", "contemplacao")
                .body(fianca).post("/parceiro/contemplacao/garantias/fiancas").then().statusCode(200).extract().jsonPath().getLong("id");
        assertEquals(primeira, segunda);
        given().queryParam("valor", 40000).get("/parceiro/contemplacao/garantias/cotas/2001-0007/cobertura")
                .then().statusCode(200).body("suficiente", is(true));
    }

    // ------------------------------------------------------------------ apoio

    private long abrirProcesso(BigDecimal valor) {
        return post("/backoffice/abertura/imoveis", mapa(
                "cpfTitular", CPF_TITULAR, "nomeTitular", "Consorciado Exemplo", "tipoOperacao", "PRONTO",
                "matricula", "12345", "cartorioRi", "1º RI de São Paulo", "inscricaoIptu", "000.000.0000-0",
                "uf", "SP", "municipio", "São Paulo", "construcaoAverbada", true, "valorNegociado", valor,
                "contatoAcesso", Map.of("nome", "Zelador", "telefone", "11 99999-0000")), "ana").getLong("id");
    }

    private static Map<String, Object> mapa(Object... chaveValor) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < chaveValor.length; i += 2) {
            m.put((String) chaveValor[i], chaveValor[i + 1]);
        }
        return m;
    }

    private static JsonPath post(String path, Object body, String usuario) {
        return given().contentType(ContentType.JSON).header("X-Usuario", usuario).body(body)
                .post(path).then().statusCode(anyOf(is(200), is(204))).extract().jsonPath();
    }

    private static JsonPath get(String path) {
        return given().get(path).then().statusCode(200).extract().jsonPath();
    }

    private static void falha(String path, Object body, String usuario, String codigo) {
        given().contentType(ContentType.JSON).header("X-Usuario", usuario).body(body)
                .post(path).then().statusCode(409).body("codigo", is(codigo));
    }

    private static void concluir(long processo, String fase) {
        post("/backoffice/processos/" + processo + "/etapas/" + fase + "/conclusao", Map.of(), "ana");
    }

    private static void assertEstado(long processo, String estado) {
        given().get("/backoffice/processos/" + processo).then().statusCode(200).body("estado", is(estado));
    }

    private static BigDecimal saldoNoCore(String cotaRef) throws Exception {
        try (Connection c = AmbienteExterno.conexaoCore();
             PreparedStatement ps = c.prepareStatement("SELECT saldo_utilizavel FROM cota WHERE cota_ref = ?")) {
            ps.setString(1, cotaRef);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1).setScale(2);
            }
        }
    }
}
