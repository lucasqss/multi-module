package com.example.processo;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O mesmo cenário para cada tipo de processo: a máquina de estados é do motor e vale igual para todos. Tipo novo
 * entra só em {@link #tipos()}.
 */
@QuarkusIntegrationTest
@WithTestResource(SistemaDePdfFalso.class)
@WithTestResource(CadastroFalso.class)
class CicloDeVidaPorTipoIT {

    /** Onde o tipo é criado e consultado, um corpo que o processamento aprova e outro que ele recusa. */
    record Tipo(String nome, String caminho, Supplier<Map<String, Object>> aprovavel,
                Supplier<Map<String, Object>> recusavel, String motivoDaRecusa) {

        @Override
        public String toString() {
            return nome;
        }
    }

    static final Map<String, String> PROPONENTE = Map.of("nome", "Ana Souza", "cpf", "52998224725");

    static final Tipo IMOBILIARIO = new Tipo("IMOBILIARIO", "/processos/imobiliarios",
            () -> imobiliario("400000.00", "500000.00"),
            () -> imobiliario("1200000.00", "2000000.00"),
            "Valor acima de 1000000.00 exige análise manual");

    static final Tipo VEICULAR = new Tipo("VEICULAR", "/processos/veiculares",
            () -> veicular(LocalDate.now().getYear() - 2),
            () -> veicular(LocalDate.now().getYear() - 11),
            "Veículo com mais de 10 anos (fabricado em " + (LocalDate.now().getYear() - 11) + ")");

    static Stream<Tipo> tipos() {
        return Stream.of(IMOBILIARIO, VEICULAR);
    }

    static Map<String, Object> imobiliario(String valor, String avaliacao) {
        var matricula = Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
        return Map.of("proponente", PROPONENTE, "valor", valor,
                "imovel", Map.of("matricula", matricula, "tipo", "RESIDENCIAL", "valorAvaliacao", avaliacao, "uf", "SP"));
    }

    static Map<String, Object> veicular(int anoFabricacao) {
        return Map.of("proponente", PROPONENTE, "valor", "45000.00",
                "veiculo", Map.of("placa", "ABC1D23", "anoFabricacao", anoFabricacao, "valorFipe", "50000.00"));
    }

    static String criar(Tipo tipo, Map<String, Object> corpo) {
        return given().contentType(ContentType.JSON).body(corpo).post(tipo.caminho())
                .then().statusCode(201).extract().path("id");
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void aprovadoTerminaConcluidoEFinal(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());

        given().post("/processos/" + id + "/processamento").then().statusCode(200)
                .body("situacao", equalTo("CONCLUIDO"))
                .body("motivo", nullValue());

        given().get(tipo.caminho() + "/" + id).then().statusCode(200).body("situacao", equalTo("CONCLUIDO"));
        given().post("/processos/" + id + "/processamento").then().statusCode(422)
                .body("detail", equalTo("Processo CONCLUIDO não pode ser processado"));
        given().post("/processos/" + id + "/cancelamento").then().statusCode(422)
                .body("detail", equalTo("Processo CONCLUIDO não pode ser cancelado"));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void recusadoGuardaOMotivoDoTipo(Tipo tipo) {
        var id = criar(tipo, tipo.recusavel().get());

        given().post("/processos/" + id + "/processamento").then().statusCode(200)
                .body("situacao", equalTo("RECUSADO"))
                .body("motivo", equalTo(tipo.motivoDaRecusa()));

        given().get(tipo.caminho() + "/" + id).then().statusCode(200)
                .body("situacao", equalTo("RECUSADO"))
                .body("motivoRecusa", equalTo(tipo.motivoDaRecusa()));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void canceladoNaoPodeSerProcessado(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());

        given().post("/processos/" + id + "/cancelamento").then().statusCode(204);

        given().get(tipo.caminho() + "/" + id).then().body("situacao", equalTo("CANCELADO"));
        given().post("/processos/" + id + "/processamento").then().statusCode(422)
                .body("detail", equalTo("Processo CANCELADO não pode ser processado"));
    }

    /**
     * Várias requisições de processamento para o mesmo processo, soltas ao mesmo tempo. Só uma pode ganhar a troca
     * ABERTO → EM_PROCESSAMENTO (update condicional no Oracle). As outras recebem 409, se tentaram o update
     * junto com a vencedora, ou 422, se já leram o processo em EM_PROCESSAMENTO ou CONCLUIDO.
     */
    @ParameterizedTest
    @MethodSource("tipos")
    void processamentoSimultaneoSoAconteceUmaVez(Tipo tipo) throws Exception {
        int requisicoes = 8;
        var id = criar(tipo, tipo.aprovavel().get());
        var largada = new CountDownLatch(1);
        var respostas = new ArrayList<Future<Integer>>();

        try (var executor = Executors.newFixedThreadPool(requisicoes)) {
            for (int i = 0; i < requisicoes; i++) {
                respostas.add(executor.submit(() -> {
                    largada.await();
                    return given().post("/processos/" + id + "/processamento").then().extract().statusCode();
                }));
            }
            largada.countDown();
            var status = new ArrayList<Integer>();
            for (var resposta : respostas) {
                status.add(resposta.get(30, TimeUnit.SECONDS));
            }

            var resumo = tipo + " " + status;
            System.out.println("Processamento simultâneo: " + resumo);
            assertEquals(1, status.stream().filter(s -> s == 200).count(), "exatamente um 200: " + resumo);
            assertTrue(status.stream().allMatch(s -> s == 200 || s == 409 || s == 422), "só 200, 409 ou 422: " + resumo);
        }
        given().get(tipo.caminho() + "/" + id).then().body("situacao", equalTo("CONCLUIDO"));
    }

    @ParameterizedTest
    @MethodSource("tipos")
    void listagemTrazOTipoComASituacaoAtual(Tipo tipo) {
        var id = criar(tipo, tipo.aprovavel().get());
        given().post("/processos/" + id + "/processamento").then().statusCode(200);

        var item = Listagem.todos("CONCLUIDO").stream().filter(i -> id.equals(i.get("id"))).findFirst()
                .orElseThrow(() -> new AssertionError("Processo " + id + " não está na listagem"));
        assertEquals(tipo.nome(), item.get("segmento"));
    }

    @Test
    void processoInexistente() {
        var inexistente = "00000000-0000-0000-0000-000000000000";
        given().post("/processos/" + inexistente + "/processamento").then().statusCode(404);
        given().post("/processos/" + inexistente + "/cancelamento").then().statusCode(404);
    }
}
