package com.example.processo.app.adapter.inbound.rest.exception;

import com.example.processo.app.adapter.inbound.rest.dto.ErroResponse;
import com.example.processo.core.motor.domain.exception.ConflitoDeSituacaoException;
import com.example.processo.core.motor.domain.exception.ProcessoNaoEncontradoException;
import com.example.processo.core.motor.domain.exception.RegraVioladaException;
import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.state.TransicaoInvalidaException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Exceções do domínio → status HTTP, para os endpoints de todos os segmentos. Corpo sempre {@link ErroResponse}. */
public class TratadorDeErros {

    @ServerExceptionMapper
    public Response regraViolada(RegraVioladaException e) {
        return erro(422, e);
    }

    @ServerExceptionMapper
    public Response transicaoInvalida(TransicaoInvalidaException e) {
        return erro(422, e);
    }

    @ServerExceptionMapper
    public Response naoEncontrado(ProcessoNaoEncontradoException e) {
        return erro(404, e);
    }

    @ServerExceptionMapper
    public Response conflito(ConflitoDeSituacaoException e) {
        return erro(409, e);
    }

    /** Parâmetro de consulta inválido (ex.: paginação fora dos limites), com o mesmo corpo dos outros erros. */
    @ServerExceptionMapper
    public Response parametroInvalido(BadRequestException e) {
        return erro(400, e);
    }

    @ServerExceptionMapper
    public Response indisponivel(SistemaExternoIndisponivelException e) {
        return erro(503, e);
    }

    private static Response erro(int status, RuntimeException e) {
        return Response.status(status).entity(new ErroResponse(e.getMessage())).build();
    }
}
