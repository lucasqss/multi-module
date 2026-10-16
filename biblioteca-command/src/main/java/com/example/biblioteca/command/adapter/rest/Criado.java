package com.example.biblioteca.command.adapter.rest;

import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Corpo do 201: só o id. Quem quiser ler o recurso consulta o lado query. */
public record Criado(UUID id) {

    static Response em(String caminho, UUID id) {
        return Response.created(URI.create(caminho + "/" + id)).entity(new Criado(id)).build();
    }
}
