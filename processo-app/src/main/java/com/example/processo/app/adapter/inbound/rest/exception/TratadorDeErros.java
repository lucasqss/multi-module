package com.example.processo.app.adapter.inbound.rest.exception;

import com.example.processo.app.adapter.inbound.rest.dto.Problema;
import com.example.processo.core.cadastro.agencia.AgenciaNaoEncontradaException;
import com.example.processo.core.motor.domain.exception.ConflitoDeSituacaoException;
import com.example.processo.core.motor.domain.exception.ProcessoNaoEncontradoException;
import com.example.processo.core.motor.domain.exception.RegraVioladaException;
import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.state.TransicaoInvalidaException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Exceções → Problem Details (RFC 9457), para os endpoints de todos os segmentos. Cada tipo de problema tem um
 * {@code type} fixo: é por ele, e não pelo texto, que um cliente decide o que fazer.
 */
public class TratadorDeErros {

    @ServerExceptionMapper
    public Response regraViolada(RegraVioladaException e) {
        return problema(422, "regra-violada", "Regra de negócio violada", e.getMessage());
    }

    @ServerExceptionMapper
    public Response transicaoInvalida(TransicaoInvalidaException e) {
        return problema(422, "transicao-invalida", "Transição de situação inválida", e.getMessage());
    }

    @ServerExceptionMapper
    public Response naoEncontrado(ProcessoNaoEncontradoException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", e.getMessage());
    }

    @ServerExceptionMapper
    public Response naoEncontrado(AgenciaNaoEncontradaException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", e.getMessage());
    }

    /** Detalhe por id inexistente, caminho desconhecido ou enum inválido em @QueryParam: o mesmo formato. */
    @ServerExceptionMapper
    public Response naoEncontrado(NotFoundException e) {
        return problema(404, "nao-encontrado", "Recurso não encontrado", "Recurso não encontrado");
    }

    @ServerExceptionMapper
    public Response conflito(ConflitoDeSituacaoException e) {
        return problema(409, "conflito", "Conflito de concorrência", e.getMessage());
    }

    @ServerExceptionMapper
    public Response parametroInvalido(BadRequestException e) {
        return problema(400, "parametro-invalido", "Parâmetro inválido", e.getMessage());
    }

    @ServerExceptionMapper
    public Response indisponivel(SistemaExternoIndisponivelException e) {
        return problema(503, "sistema-indisponivel", "Sistema externo indisponível", e.getMessage());
    }

    private static Response problema(int status, String tipo, String titulo, String detalhe) {
        return Response.status(status)
                .type(Problema.MEDIA_TYPE)
                .entity(new Problema("/problemas/" + tipo, titulo, status, detalhe))
                .build();
    }
}
