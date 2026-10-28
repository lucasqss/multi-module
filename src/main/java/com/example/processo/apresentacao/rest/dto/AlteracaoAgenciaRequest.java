package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Agencia;

/** Corpo do PUT /agencias/{codigo}: o código vem do caminho e não muda. Sem {@code ativa}, fica ativa. */
public record AlteracaoAgenciaRequest(String nome, String uf, Boolean ativa) {

    public Agencia paraModelo(String codigo) {
        return new Agencia(codigo, nome, uf, ativa == null || ativa);
    }
}
