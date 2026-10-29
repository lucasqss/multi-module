package br.com.consorcio.utilizacao.rest.erro;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Traduz exceções de domínio para HTTP, igual para todos os públicos. */
public class ErrosRest {

    public record Erro(String codigo, String mensagem) {
    }

    @ServerExceptionMapper
    public Response regra(RegraNegocioException e) {
        return Response.status(Response.Status.CONFLICT).entity(new Erro(e.codigo(), e.getMessage())).build();
    }

    @ServerExceptionMapper
    public Response naoEncontrado(NaoEncontradoException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(new Erro("NAO_ENCONTRADO", e.getMessage())).build();
    }
}
