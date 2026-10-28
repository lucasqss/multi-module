package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.NovoProcessoVeicularRequest;
import com.example.processo.apresentacao.rest.dto.ProcessoCriadoResponse;
import com.example.processo.negocio.processo.ProcessoService;
import com.example.processo.persistencia.consulta.DetalheVeicular;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Endpoints da modalidade veicular: mesmo formato do imobiliário, mesmo motor. */
@Path(ProcessoVeicularResource.CAMINHO)
public class ProcessoVeicularResource {

    static final String CAMINHO = "/processos/veiculares";

    private final ProcessoService motor;
    private final ProcessoConsultas consultas;

    ProcessoVeicularResource(ProcessoService motor, ProcessoConsultas consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoVeicularRequest request) {
        var criado = ProcessoCriadoResponse.de(motor.criar(request.paraModelo()));
        return Response.created(URI.create(CAMINHO + "/" + criado.id())).entity(criado).build();
    }

    @GET
    @Path("{id}")
    public DetalheVeicular detalhe(@PathParam("id") UUID id) {
        return consultas.veicular(id).orElseThrow(NotFoundException::new);
    }
}
