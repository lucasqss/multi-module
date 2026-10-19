package com.example.processo.app.rest;

import com.example.processo.core.motor.domain.Processo;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Corpo do 201 de qualquer segmento: o id e o número gerado. O Location aponta para a consulta do processo. */
public record Criado(UUID id, String numero) {

    static Response em(String caminho, Processo<?> processo) {
        return Response.created(URI.create(caminho + "/" + processo.id()))
                .entity(new Criado(processo.id(), processo.numero()))
                .build();
    }
}
