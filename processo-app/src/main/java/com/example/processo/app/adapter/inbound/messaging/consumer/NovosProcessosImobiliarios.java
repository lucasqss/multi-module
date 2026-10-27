package com.example.processo.app.adapter.inbound.messaging.consumer;

import com.example.processo.app.adapter.inbound.messaging.dto.NovoProcessoImobiliarioMensagem;
import com.example.processo.app.adapter.inbound.messaging.dto.RespostaNovoProcesso;
import com.example.processo.core.motor.application.service.MotorDeProcessamento;
import com.example.processo.core.motor.domain.exception.RegraVioladaException;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Cria processo imobiliário a partir da fila: a mesma entrada do POST /processos/imobiliarios, por outro canal.
 * Traduz a mensagem, chama o motor e decide o destino da mensagem pelo tipo do erro:
 *
 * <ul>
 *   <li>criado, ou já criado antes com o mesmo id: responde {@code criado} e confirma (ack);</li>
 *   <li>regra violada ou mensagem malformada: responde {@code recusado} e confirma; reentregar não mudaria o
 *       resultado;</li>
 *   <li>qualquer outra falha (sistema externo fora do ar, banco): devolve ao broker (nack) para reentrega. O id da
 *       mensagem é a chave de idempotência, então a reentrega não duplica o processo.</li>
 * </ul>
 *
 * Não abre transação: o motor já usa as dele, curtas.
 */
@ApplicationScoped
public class NovosProcessosImobiliarios {

    private static final Logger LOG = Logger.getLogger(NovosProcessosImobiliarios.class);

    private final MotorDeProcessamento motor;
    private final Emitter<RespostaNovoProcesso> respostas;

    NovosProcessosImobiliarios(MotorDeProcessamento motor,
                               @Channel("processos-imobiliarios-respostas") Emitter<RespostaNovoProcesso> respostas) {
        this.motor = motor;
        this.respostas = respostas;
    }

    /**
     * {@code @ActivateRequestContext}: fora do HTTP não há request context, e as leituras do motor fora de transação
     * (chave de idempotência, carregar o processo) usam a StatelessSession, que precisa de um. Todo canal de entrada
     * que não seja REST (fila, agendamento) precisa disso.
     */
    @Incoming("processos-imobiliarios-novos")
    @Blocking
    @ActivateRequestContext
    public CompletionStage<Void> consumir(Message<Object> mensagem) {
        NovoProcessoImobiliarioMensagem pedido;
        try {
            pedido = json(mensagem.getPayload()).mapTo(NovoProcessoImobiliarioMensagem.class);
        } catch (RuntimeException e) {
            LOG.warnf(e, "Mensagem malformada descartada: %s", mensagem.getPayload());
            respostas.send(RespostaNovoProcesso.recusado(null, "Mensagem malformada"));
            return mensagem.ack();
        }
        if (pedido.id() == null || pedido.id().isBlank()) {
            respostas.send(RespostaNovoProcesso.recusado(null, "Mensagem sem id"));
            return mensagem.ack();
        }

        try {
            var processo = motor.criar(pedido.paraSolicitacao(), pedido.id());
            respostas.send(RespostaNovoProcesso.criado(pedido.id(), processo));
            return mensagem.ack();
        } catch (RegraVioladaException e) {
            respostas.send(RespostaNovoProcesso.recusado(pedido.id(), e.getMessage()));
            return mensagem.ack();
        } catch (RuntimeException e) {
            LOG.warnf(e, "Mensagem %s devolvida para reentrega", pedido.id());
            return mensagem.nack(e);
        }
    }

    /**
     * O connector entrega JSON como {@code JsonObject} quando a mensagem vem com content-type application/json, e como
     * texto ou bytes quando não vem (o comum num produtor de mainframe). Os três formatos são aceitos.
     */
    static JsonObject json(Object payload) {
        return switch (payload) {
            case JsonObject json -> json;
            case String texto -> new JsonObject(texto);
            case Buffer buffer -> buffer.toJsonObject();
            case byte[] bytes -> Buffer.buffer(bytes).toJsonObject();
            default -> throw new IllegalArgumentException("Payload não suportado: " + payload.getClass().getName());
        };
    }
}
