package com.example.processo.app.adapter.inbound.rest.resource;

import com.example.processo.app.adapter.inbound.rest.dto.NovoProcessoVeicularRequest;
import com.example.processo.app.adapter.inbound.rest.dto.ProcessoCriadoResponse;
import com.example.processo.app.adapter.inbound.rest.mapper.ProcessoRestMapper;
import com.example.processo.app.adapter.inbound.rest.mapper.ProcessoVeicularRestMapper;
import com.example.processo.core.motor.application.service.MotorDeProcessamento;
import com.example.processo.query.dto.DetalheVeicular;
import com.example.processo.query.repository.ProcessoQueries;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/** Endpoints do segmento veicular: mesmo formato do imobiliário, mesmo motor. */
@Path(ProcessoVeicularResource.CAMINHO)
public class ProcessoVeicularResource {

    static final String CAMINHO = "/processos/veiculares";

    private final MotorDeProcessamento motor;
    private final ProcessoQueries consultas;

    ProcessoVeicularResource(MotorDeProcessamento motor, ProcessoQueries consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoVeicularRequest request) {
        ProcessoCriadoResponse criado = ProcessoRestMapper.paraCriado(
                motor.criar(ProcessoVeicularRestMapper.paraSolicitacao(request)));
        return Response.created(URI.create(CAMINHO + "/" + criado.id())).entity(criado).build();
    }

    @GET
    @Path("{id}")
    public DetalheVeicular detalhe(@PathParam("id") UUID id) {
        return consultas.veicular(id).orElseThrow(NotFoundException::new);
    }
}
