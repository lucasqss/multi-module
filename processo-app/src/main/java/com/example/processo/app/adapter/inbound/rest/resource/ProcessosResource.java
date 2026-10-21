package com.example.processo.app.adapter.inbound.rest.resource;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.app.adapter.inbound.rest.dto.SituacaoResponse;
import com.example.processo.app.adapter.inbound.rest.mapper.ProcessoRestMapper;
import com.example.processo.core.motor.application.service.MotorDeProcessamento;
import com.example.processo.query.dto.ResumoProcesso;
import com.example.processo.query.repository.ProcessoQueries;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.UUID;

/**
 * O que vale para todos os segmentos: a listagem e o ciclo de vida (processar, cancelar), que é do motor. A
 * criação e o detalhe são por segmento, no resource de cada um, porque o corpo é do segmento.
 */
@Path("/processos")
public class ProcessosResource {

    private final ProcessoQueries consultas;
    private final MotorDeProcessamento motor;

    ProcessosResource(ProcessoQueries consultas, MotorDeProcessamento motor) {
        this.consultas = consultas;
        this.motor = motor;
    }

    /** {@code ?situacao=ABERTO} filtra; valor fora do enum responde 404, como manda o JAX-RS para @QueryParam. */
    @GET
    public List<ResumoProcesso> listar(@QueryParam("situacao") SituacaoProcesso situacao) {
        return situacao == null ? consultas.todos() : consultas.naSituacao(situacao);
    }

    @POST
    @Path("{id}/processamento")
    public SituacaoResponse processar(@PathParam("id") UUID id) {
        return ProcessoRestMapper.paraSituacao(motor.processar(id));
    }

    @POST
    @Path("{id}/cancelamento")
    public void cancelar(@PathParam("id") UUID id) {
        motor.cancelar(id);
    }
}
