package com.example.consorcio.entrypoint.api.v1.handler;

import com.example.consorcio.domain.exception.DomainException;
import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Traduz exceções do domínio para HTTP. O domínio nunca sabe o que é um status 422. */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException e) {
        int status = e instanceof RecursoNaoEncontradoException ? 404 : 422;
        return Response.status(status).entity(new ErroResponse(e.getCodigo(), e.getMessage())).build();
    }
}
