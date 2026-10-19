package com.example.processo.app.rest;

import com.example.processo.core.motor.domain.RegraVioladaException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Vale para os endpoints de todos os segmentos. */
public class ErrosMapper {

    public record Erro(String motivo) {
    }

    @ServerExceptionMapper
    public Response regraViolada(RegraVioladaException e) {
        return Response.status(422).entity(new Erro(e.getMessage())).build();
    }
}
