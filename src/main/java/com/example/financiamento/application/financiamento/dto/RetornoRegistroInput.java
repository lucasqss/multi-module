package com.example.financiamento.application.financiamento.dto;

import com.example.financiamento.domain.valueobject.IdProtocolo;
import com.example.financiamento.domain.valueobject.RetornoRegistro;

import java.time.Instant;

/** Retorno do órgão de registro, já traduzido do formato dele pelo entrypoint. */
public record RetornoRegistroInput(String idProtocolo, boolean registrado, String identificacaoRegistrada,
                                   String numeroRegistro, String motivoRecusa, Instant momento) {

    public RetornoRegistro retorno() {
        return new RetornoRegistro(IdProtocolo.de(idProtocolo), registrado, identificacaoRegistrada, numeroRegistro,
                motivoRecusa, momento);
    }
}
