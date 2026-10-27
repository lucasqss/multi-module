package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.cadastro.agencia.Agencia;

/** Corpo do POST /agencias. Sem {@code ativa}, a agência nasce ativa. */
public record NovaAgenciaRequest(String codigo, String nome, String uf, Boolean ativa) {

    public Agencia paraDominio() {
        return new Agencia(codigo, nome, uf, ativa == null || ativa);
    }
}
