package com.example.processo.app.adapter.inbound.rest.resource;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.app.adapter.inbound.rest.dto.PaginaResponse;
import com.example.processo.app.adapter.inbound.rest.dto.SituacaoResponse;
import com.example.processo.core.motor.application.service.MotorDeProcessamento;
import com.example.processo.query.dto.ResumoProcesso;
import com.example.processo.query.repository.ProcessoQueries;
import jakarta.data.page.PageRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.util.UUID;

/**
 * O que vale para todos os segmentos: a listagem e o ciclo de vida (processar, cancelar), que é do motor. A
 * criação e o detalhe são por segmento, no resource de cada um, porque o corpo é do segmento.
 */
@Path("/processos")
public class ProcessosResource {

    static final int TAMANHO_MAXIMO = 100;

    private final ProcessoQueries consultas;
    private final MotorDeProcessamento motor;

    ProcessosResource(ProcessoQueries consultas, MotorDeProcessamento motor) {
        this.consultas = consultas;
        this.motor = motor;
    }

    /**
     * {@code ?situacao=ABERTO} filtra; valor fora do enum responde 404, como manda o JAX-RS para @QueryParam.
     * {@code ?pagina=1&tamanho=20} pagina (padrões); fora de {@code pagina >= 1} e {@code 1..100} responde 400.
     */
    @GET
    public PaginaResponse<ResumoProcesso> listar(@QueryParam("situacao") SituacaoProcesso situacao,
                                                 @QueryParam("pagina") @DefaultValue("1") int pagina,
                                                 @QueryParam("tamanho") @DefaultValue("20") int tamanho) {
        if (pagina < 1 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new BadRequestException("pagina deve ser >= 1 e tamanho entre 1 e " + TAMANHO_MAXIMO);
        }
        var pedido = PageRequest.ofPage(pagina, tamanho, true);
        return PaginaResponse.de(situacao == null ? consultas.todos(pedido) : consultas.naSituacao(situacao, pedido));
    }

    @POST
    @Path("{id}/processamento")
    public SituacaoResponse processar(@PathParam("id") UUID id) {
        return SituacaoResponse.de(motor.processar(id));
    }

    @POST
    @Path("{id}/cancelamento")
    public void cancelar(@PathParam("id") UUID id) {
        motor.cancelar(id);
    }
}
