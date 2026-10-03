package com.example.biblioteca.command.adapter.rest;

import com.example.biblioteca.command.domain.NaoEncontradoException;
import com.example.biblioteca.command.domain.RegraVioladaException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ErrosMapper {

    public record Erro(String motivo) {
    }

    @ServerExceptionMapper
    public Response regraViolada(RegraVioladaException e) {
        return Response.status(422).entity(new Erro(e.getMessage())).build();
    }

    @ServerExceptionMapper
    public Response naoEncontrado(NaoEncontradoException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(new Erro(e.getMessage())).build();
    }
}
