package com.example.modelo.app;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Traduz as exceções do core para HTTP: o core não conhece REST. */
public class ErrosMapper {

    @ServerExceptionMapper
    public Response argumentoInvalido(IllegalArgumentException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("motivo", e.getMessage())).build();
    }
}
