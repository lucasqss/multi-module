package com.example.processo.app.rest;

import com.example.processo.core.motor.domain.ConflitoDeSituacaoException;
import com.example.processo.core.motor.domain.ProcessoNaoEncontradoException;
import com.example.processo.core.motor.domain.RegraVioladaException;
import com.example.processo.core.motor.domain.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.estado.TransicaoInvalidaException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Vale para os endpoints de todos os segmentos. */
public class ErrosMapper {

    public record Erro(String motivo) {
    }

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

    @ServerExceptionMapper
    public Response indisponivel(SistemaExternoIndisponivelException e) {
        return erro(503, e);
    }

    private static Response erro(int status, RuntimeException e) {
        return Response.status(status).entity(new Erro(e.getMessage())).build();
    }
}
