package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Processo;
import com.example.processo.modelo.Situacao;

/** Resultado de processar: a nova situação e, se recusado, o motivo. */
public record SituacaoResponse(Situacao situacao, String motivo) {

    public static SituacaoResponse de(Processo processo) {
        return new SituacaoResponse(processo.getSituacao(), processo.getMotivoRecusa());
    }
}
