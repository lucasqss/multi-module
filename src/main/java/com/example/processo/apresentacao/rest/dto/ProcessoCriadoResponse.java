package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Processo;

import java.util.UUID;

/** Corpo do 201 de qualquer modalidade: o id e o número gerado. */
public record ProcessoCriadoResponse(UUID id, String numero) {

    public static ProcessoCriadoResponse de(Processo processo) {
        return new ProcessoCriadoResponse(processo.getId(), processo.getNumero());
    }
}
