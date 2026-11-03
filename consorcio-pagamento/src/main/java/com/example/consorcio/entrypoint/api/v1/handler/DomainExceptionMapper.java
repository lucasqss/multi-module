package com.example.consorcio.entrypoint.api.v1.handler;

import com.example.consorcio.domain.exception.DomainException;
import com.example.consorcio.domain.exception.RecursoNaoEncontradoException;
import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.exception.TransicaoEstadoInvalidaException;
import com.example.consorcio.domain.exception.Violacao;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

/**
 * Traduz exceções do domínio para HTTP (o domínio nunca sabe o que é um status code):
 * <ul>
 *   <li>404 — recurso não encontrado</li>
 *   <li>409 — ação incompatível com o estado atual (State pattern)</li>
 *   <li>422 — regra de negócio violada (Specification pattern), com a lista de violações</li>
 * </ul>
 */
@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException e) {
        int status = switch (e) {
            case RecursoNaoEncontradoException ignored -> 404;
            case TransicaoEstadoInvalidaException ignored -> 409;
            default -> 422;
        };
        List<Violacao> violacoes = e instanceof RegraNegocioException regra ? regra.getViolacoes() : List.of();
        return Response.status(status)
                .entity(new ErroResponse(e.getCodigo(), e.getMessage(), violacoes))
                .build();
    }
}
