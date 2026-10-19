package com.example.processo.app.rest;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.query.ProcessoQueries;
import com.example.processo.query.ResumoProcesso;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.List;

/** Listagem de todos os segmentos. O detalhe é por segmento, no endpoint de cada um. */
@Path("/processos")
public class ProcessosResource {

    private final ProcessoQueries consultas;

    ProcessosResource(ProcessoQueries consultas) {
        this.consultas = consultas;
    }

    /** {@code ?situacao=ABERTO} filtra; valor fora do enum responde 404, como manda o JAX-RS para @QueryParam. */
    @GET
    public List<ResumoProcesso> listar(@QueryParam("situacao") SituacaoProcesso situacao) {
        return situacao == null ? consultas.todos() : consultas.naSituacao(situacao);
    }
}
