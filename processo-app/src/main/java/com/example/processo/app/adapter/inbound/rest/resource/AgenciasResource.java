package com.example.processo.app.adapter.inbound.rest.resource;

import com.example.processo.app.adapter.inbound.rest.dto.AlteracaoAgenciaRequest;
import com.example.processo.app.adapter.inbound.rest.dto.NovaAgenciaRequest;
import com.example.processo.core.cadastro.agencia.Agencia;
import com.example.processo.core.cadastro.agencia.Agencias;
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
 * CRUD de agências: o resource chama o serviço do cadastro e devolve o próprio record, que é só dados. Compare com os
 * resources de processo, que passam pelo motor: aqui não há fluxo, então não há motor.
 */
@Path("/agencias")
public class AgenciasResource {

    private final Agencias agencias;

    AgenciasResource(Agencias agencias) {
        this.agencias = agencias;
    }

    @GET
    public List<Agencia> listar() {
        return agencias.todas();
    }

    @GET
    @Path("{codigo}")
    public Agencia porCodigo(@PathParam("codigo") String codigo) {
        return agencias.porCodigo(codigo);
    }

    @POST
    public Response criar(NovaAgenciaRequest request) {
        var criada = agencias.criar(request.paraDominio());
        return Response.created(URI.create("/agencias/" + criada.codigo())).entity(criada).build();
    }

    @PUT
    @Path("{codigo}")
    public Agencia alterar(@PathParam("codigo") String codigo, AlteracaoAgenciaRequest request) {
        return agencias.alterar(request.paraDominio(codigo));
    }

    @DELETE
    @Path("{codigo}")
    public void excluir(@PathParam("codigo") String codigo) {
        agencias.excluir(codigo);
    }
}
