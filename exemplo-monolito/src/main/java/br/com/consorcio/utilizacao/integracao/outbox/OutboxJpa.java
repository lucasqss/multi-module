package br.com.consorcio.utilizacao.integracao.outbox;

import br.com.consorcio.utilizacao.compartilhado.saida.SaidaAssincrona;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Implementa a porta do compartilhado. Nunca chama o externo: só grava a intenção. */
@ApplicationScoped
public class OutboxJpa implements SaidaAssincrona {

    @Inject
    MensagemOutboxRepository mensagens;

    @Inject
    ObjectMapper json;

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void registrar(String tipo, Long referenciaId, String chaveIdempotencia, Object payload) {
        try {
            mensagens.persist(new MensagemOutbox(tipo, referenciaId, chaveIdempotencia, json.writeValueAsString(payload)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Payload inválido para " + tipo, e);
        }
    }
}
