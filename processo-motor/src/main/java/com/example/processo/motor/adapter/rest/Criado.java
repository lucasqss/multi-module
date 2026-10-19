package com.example.processo.motor.adapter.rest;

import com.example.processo.motor.domain.Processo;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Corpo do 201 de qualquer segmento: o id e o número gerado. */
public record Criado(UUID id, String numero) {

    public static Response de(Processo<?> processo) {
        return Response.created(URI.create("/processos/" + processo.id()))
                .entity(new Criado(processo.id(), processo.numero()))
                .build();
    }
}
