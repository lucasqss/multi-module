package com.example.processo.app.adapter.inbound.messaging.dto;

import com.example.processo.core.motor.domain.model.Processo;

import java.util.UUID;

/**
 * Resposta publicada em processos.imobiliarios.respostas para cada mensagem tratada: o processo criado ou o motivo da
 * recusa. {@code idMensagem} liga a resposta ao pedido.
 */
public record RespostaNovoProcesso(String idMensagem, boolean criado, UUID processoId, String numero, String motivo) {

    public static RespostaNovoProcesso criado(String idMensagem, Processo<?> processo) {
        return new RespostaNovoProcesso(idMensagem, true, processo.id(), processo.numero(), null);
    }

    public static RespostaNovoProcesso recusado(String idMensagem, String motivo) {
        return new RespostaNovoProcesso(idMensagem, false, null, null, motivo);
    }
}
