package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.motor.domain.model.Processo;

import java.util.UUID;

/** Corpo do 201 de qualquer segmento: o id e o número gerado. */
public record ProcessoCriadoResponse(UUID id, String numero) {

    public static ProcessoCriadoResponse de(Processo<?> processo) {
        return new ProcessoCriadoResponse(processo.id(), processo.numero());
    }
}
