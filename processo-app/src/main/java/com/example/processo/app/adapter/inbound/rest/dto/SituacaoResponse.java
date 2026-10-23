package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.state.Situacao;

/** Resultado de processar: a nova situação e, se recusado, o motivo. */
public record SituacaoResponse(Situacao situacao, String motivo) {

    public static SituacaoResponse de(Processo<?> processo) {
        return new SituacaoResponse(processo.situacao(), processo.motivoRecusa());
    }
}
