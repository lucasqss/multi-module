package com.example.processo.app.adapter.inbound.rest.resource;

import com.example.processo.app.adapter.inbound.rest.dto.NovoProcessoImobiliarioRequest;
import com.example.processo.app.adapter.inbound.rest.dto.ProcessoCriadoResponse;
import com.example.processo.app.adapter.inbound.rest.mapper.ProcessoImobiliarioRestMapper;
import com.example.processo.app.adapter.inbound.rest.mapper.ProcessoRestMapper;
import com.example.processo.core.motor.application.port.in.CriarProcessoUseCase;
import com.example.processo.query.dto.DetalheImobiliario;
import com.example.processo.query.repository.ProcessoQueries;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/**
 * Endpoints do segmento imobiliário. A criação passa pelo use case (motor único, processo-core); a consulta vai
 * direto ao lado de leitura (processo-query), cuja projeção já é o contrato de resposta.
 */
@Path(ProcessoImobiliarioResource.CAMINHO)
public class ProcessoImobiliarioResource {

    static final String CAMINHO = "/processos/imobiliarios";

    private final CriarProcessoUseCase criar;
    private final ProcessoQueries consultas;

    ProcessoImobiliarioResource(CriarProcessoUseCase criar, ProcessoQueries consultas) {
        this.criar = criar;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoImobiliarioRequest request) {
        ProcessoCriadoResponse criado = ProcessoRestMapper.paraCriado(
                criar.criar(ProcessoImobiliarioRestMapper.paraSolicitacao(request)));
        return Response.created(URI.create(CAMINHO + "/" + criado.id())).entity(criado).build();
    }

    @GET
    @Path("{id}")
    public DetalheImobiliario detalhe(@PathParam("id") UUID id) {
        return consultas.imobiliario(id).orElseThrow(NotFoundException::new);
    }
}
