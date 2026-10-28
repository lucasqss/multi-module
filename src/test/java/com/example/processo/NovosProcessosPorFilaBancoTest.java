package com.example.processo;

import com.example.processo.apresentacao.mensageria.dto.RespostaNovoProcesso;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O consumidor da fila de novos processos, com os canais em memória (sem broker) e o banco real: o que vira
 * resposta, o que é confirmado (ack) e o que volta para reentrega (nack).
 */
@QuarkusTest
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class NovosProcessosPorFilaBancoTest {

    @Inject
    @Any
    InMemoryConnector fila;

    @Inject
    ProcessoConsultas consultas;

    InMemorySink<RespostaNovoProcesso> respostas;

    @BeforeEach
    void limpar() {
        respostas = fila.sink(FilaEmMemoria.RESPOSTAS);
        respostas.clear();
    }

    static JsonObject mensagem(String id, String cpf) {
        var matricula = Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
        return new JsonObject()
                .put("id", id).put("nome", "Ana Souza").put("cpf", cpf).put("valor", "400000.00")
                .put("matricula", matricula).put("tipoImovel", "RESIDENCIAL").put("valorAvaliacao", "500000.00")
                .put("uf", "SP");
    }

    /** Envia e devolve como a mensagem terminou: "ack" ou "nack". */
    CompletableFuture<String> enviar(Object corpo) {
        var desfecho = new CompletableFuture<String>();
        // Envia uma Message (não só o payload) para observar o ack/nack que o consumidor dá
        fila.<Message<Object>>source(FilaEmMemoria.NOVOS).send(Message.<Object>of(corpo,
                () -> {
                    desfecho.complete("ack");
                    return CompletableFuture.completedFuture(null);
                },
                erro -> {
                    desfecho.complete("nack");
                    return CompletableFuture.completedFuture(null);
                }));
        return desfecho;
    }

    static String esperar(CompletableFuture<String> desfecho) throws Exception {
        return desfecho.get(30, TimeUnit.SECONDS);
    }

    List<RespostaNovoProcesso> respostasDe(String id) {
        return respostas.received().stream().map(Message::getPayload).filter(r -> id.equals(r.idMensagem())).toList();
    }

    @Test
    void mensagemValidaCriaOProcessoERespondeComONumero() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "52998224725"))));

        var resposta = respostasDe(id).getFirst();
        assertTrue(resposta.criado());
        assertTrue(resposta.numero().startsWith("IMO-"));
        assertEquals(resposta.numero(), consultas.imobiliario(resposta.processoId()).orElseThrow().numero());
    }

    @Test
    void jsonComoTextoTambemValeComoChegaDoMainframe() throws Exception {
        // Sem content-type JSON, o connector AMQP entrega o corpo como texto
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "52998224725").encode())));

        assertTrue(respostasDe(id).getFirst().criado());
    }

    @Test
    void mensagemReentregueNaoDuplicaOProcesso() throws Exception {
        var id = "msg-" + UUID.randomUUID();
        var corpo = mensagem(id, "52998224725");

        assertEquals("ack", esperar(enviar(corpo)));
        assertEquals("ack", esperar(enviar(corpo)));

        var recebidas = respostasDe(id);
        assertEquals(2, recebidas.size());
        assertEquals(recebidas.get(0).processoId(), recebidas.get(1).processoId());
        assertEquals(recebidas.get(0).numero(), recebidas.get(1).numero());
    }

    @Test
    void regraVioladaRespondeRecusadoEConfirma() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("ack", esperar(enviar(mensagem(id, "12345678900"))));

        var resposta = respostasDe(id).getFirst();
        assertFalse(resposta.criado());
        assertEquals("CPF inválido: 12345678900", resposta.motivo());
    }

    @Test
    void mensagemMalformadaRespondeRecusadoEConfirma() throws Exception {
        var antes = respostas.received().size();

        assertEquals("ack", esperar(enviar(new JsonObject().put("id", "x").put("tipoImovel", "CASTELO"))));

        var resposta = respostas.received().get(antes).getPayload();
        assertFalse(resposta.criado());
        assertEquals("Mensagem malformada", resposta.motivo());
    }

    @Test
    void sistemaExternoForaDoArDevolveParaReentrega() throws Exception {
        var id = "msg-" + UUID.randomUUID();

        assertEquals("nack", esperar(enviar(mensagem(id, CadastroFalso.CPF_SISTEMA_FORA))));

        assertTrue(respostasDe(id).isEmpty(), "sem resposta: a mensagem volta para a fila");
    }
}
