package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.Problema;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.TransicaoInvalidaException;
import com.example.processo.negocio.agencia.AgenciaNaoEncontradaException;
import com.example.processo.negocio.processo.ConflitoDeSituacaoException;
import com.example.processo.negocio.processo.ProcessoNaoEncontradoException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Exceções → Problem Details (RFC 9457), para todos os endpoints. Cada tipo de problema tem um
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
