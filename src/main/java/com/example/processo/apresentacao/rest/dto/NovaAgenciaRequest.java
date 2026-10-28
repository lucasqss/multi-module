package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Agencia;

/** Corpo do POST /agencias. Sem {@code ativa}, a agência nasce ativa. */
public record NovaAgenciaRequest(String codigo, String nome, String uf, Boolean ativa) {

    public Agencia paraModelo() {
        return new Agencia(codigo, nome, uf, ativa == null || ativa);
    }
}
