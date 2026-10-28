package com.example.processo.apresentacao.rest;

import com.example.processo.apresentacao.rest.dto.PaginaResponse;
import com.example.processo.apresentacao.rest.dto.SituacaoResponse;
import com.example.processo.modelo.Situacao;
import com.example.processo.negocio.processo.ProcessoService;
import com.example.processo.persistencia.consulta.ProcessoConsultas;
import com.example.processo.persistencia.consulta.ResumoProcesso;
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
 * O que vale para todas as modalidades: a listagem e o ciclo de vida (processar, cancelar), que é do motor. A
 * criação e o detalhe são por modalidade, no resource de cada uma, porque o corpo é da modalidade.
 */
@Path("/processos")
public class ProcessosResource {

    static final int TAMANHO_MAXIMO = 100;

    private final ProcessoConsultas consultas;
    private final ProcessoService motor;

    ProcessosResource(ProcessoConsultas consultas, ProcessoService motor) {
        this.consultas = consultas;
        this.motor = motor;
    }

    /**
     * {@code ?situacao=ABERTO} filtra; valor fora do enum responde 404, como manda o JAX-RS para @QueryParam.
     * {@code ?pagina=1&tamanho=20} pagina (padrões); fora de {@code pagina >= 1} e {@code 1..100} responde 400.
     */
    @GET
    public PaginaResponse<ResumoProcesso> listar(@QueryParam("situacao") Situacao situacao,
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
