package com.example.consorcio.entrypoint.api.v1.handler;

import com.example.consorcio.domain.exception.DadoInvalidoException;
import com.example.consorcio.domain.exception.DomainException;
import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.exception.TransicaoEstadoInvalidaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduz as exceções do domínio para HTTP pela FAMÍLIA, não pelo caso concreto:
 * uma exceção nova (ex.: {@code LimitePixExcedidoException}) já sai com o status certo
 * sem mexer aqui. O domínio nunca sabe o que é um status code.
 */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException e) {
        int status = switch (e) {
            case DadoInvalidoException ignored -> 400;
            case RecursoNaoEncontradoException ignored -> 404;
            case TransicaoEstadoInvalidaException ignored -> 409;
            case RegraNegocioException ignored -> 422;
            default -> 500;
        };
        return Response.status(status)
                .entity(new ErroResponse(e.getCodigo(), e.getMessage()))
                .build();
    }
}
