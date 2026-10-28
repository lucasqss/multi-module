package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.AgenciaResponse;
import com.example.processo.apresentacao.rest.dto.AlteracaoAgenciaRequest;
import com.example.processo.apresentacao.rest.dto.NovaAgenciaRequest;
import com.example.processo.negocio.agencia.AgenciaService;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

/**
 * CRUD de agências: o resource chama o serviço do cadastro e devolve a resposta da API. Compare com os resources de
 * processo, que passam pelo motor: aqui não há fluxo, então não há motor.
 */
@Path("/agencias")
public class AgenciasResource {

    private final AgenciaService agencias;

    AgenciasResource(AgenciaService agencias) {
        this.agencias = agencias;
    }

    @GET
    public List<AgenciaResponse> listar() {
        return agencias.todas().stream().map(AgenciaResponse::de).toList();
    }

    @GET
    @Path("{codigo}")
    public AgenciaResponse porCodigo(@PathParam("codigo") String codigo) {
        return AgenciaResponse.de(agencias.porCodigo(codigo));
    }

    @POST
    public Response criar(NovaAgenciaRequest request) {
        var criada = AgenciaResponse.de(agencias.criar(request.paraModelo()));
        return Response.created(URI.create("/agencias/" + criada.codigo())).entity(criada).build();
    }

    @PUT
    @Path("{codigo}")
    public AgenciaResponse alterar(@PathParam("codigo") String codigo, AlteracaoAgenciaRequest request) {
        return AgenciaResponse.de(agencias.alterar(request.paraModelo(codigo)));
    }

    @DELETE
    @Path("{codigo}")
    public void excluir(@PathParam("codigo") String codigo) {
        agencias.excluir(codigo);
    }
}
