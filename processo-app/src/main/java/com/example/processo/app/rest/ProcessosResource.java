package com.example.processo.app.rest;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.core.motor.application.MotorDeProcessamento;
import com.example.processo.core.motor.domain.estado.Situacao;
import com.example.processo.query.ProcessoQueries;
import com.example.processo.query.ResumoProcesso;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.List;
import java.util.UUID;

/**
 * O que vale para todos os segmentos: a listagem e o ciclo de vida (processar, cancelar), que é do motor. A
 * criação e o detalhe são por segmento, no endpoint de cada um, porque o corpo é do segmento.
 */
@Path("/processos")
public class ProcessosResource {

    public record NovaSituacao(Situacao situacao, String motivo) {
    }

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
    public NovaSituacao processar(@PathParam("id") UUID id) {
        var processado = motor.processar(id);
        return new NovaSituacao(processado.situacao(), processado.motivoRecusa());
    }

    @POST
    @Path("{id}/cancelamento")
    public void cancelar(@PathParam("id") UUID id) {
        motor.cancelar(id);
    }
}
