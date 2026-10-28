package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.NovoProcessoImobiliarioRequest;
import com.example.processo.apresentacao.rest.dto.ProcessoCriadoResponse;
import com.example.processo.negocio.processo.ProcessoService;
import com.example.processo.persistencia.consulta.DetalheImobiliario;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

/**
 * Endpoints da modalidade imobiliária. A criação passa pelo motor ({@link ProcessoService}); o detalhe vai direto à
 * consulta da persistência, cuja projeção já é o contrato de resposta.
 */
@Path(ProcessoImobiliarioResource.CAMINHO)
public class ProcessoImobiliarioResource {

    static final String CAMINHO = "/processos/imobiliarios";

    private final ProcessoService motor;
    private final ProcessoConsultas consultas;

    ProcessoImobiliarioResource(ProcessoService motor, ProcessoConsultas consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    @POST
    public Response criar(NovoProcessoImobiliarioRequest request) {
        var criado = ProcessoCriadoResponse.de(motor.criar(request.paraModelo()));
        return Response.created(URI.create(CAMINHO + "/" + criado.id())).entity(criado).build();
    }

    @GET
    @Path("{id}")
    public DetalheImobiliario detalhe(@PathParam("id") UUID id) {
        return consultas.imobiliario(id).orElseThrow(NotFoundException::new);
    }
}
