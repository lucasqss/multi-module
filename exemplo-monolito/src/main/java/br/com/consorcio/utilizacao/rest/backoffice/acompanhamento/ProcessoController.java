package br.com.consorcio.utilizacao.rest.backoffice.acompanhamento;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

/** Acompanhamento e avanço de etapas, comum a qualquer modalidade. */
@Path("/backoffice/processos")
public class ProcessoController {

    public record Cancelamento(String motivo) {
    }

    @Inject
    ProcessoApi processos;

    @GET
    @Path("/{id}")
    public ProcessoDto obter(@PathParam("id") Long id) {
        return ProcessoDto.de(processos.linhaDoTempo(id));
    }

    @POST
    @Path("/{id}/etapas/{fase}/conclusao")
    public void concluir(@PathParam("id") Long id, @PathParam("fase") Fase fase, @HeaderParam("X-Usuario") String usuario) {
        processos.concluirEtapa(id, fase, usuario);
    }

    @POST
    @Path("/{id}/cancelamento")
    public void cancelar(@PathParam("id") Long id, Cancelamento req, @HeaderParam("X-Usuario") String usuario) {
        processos.cancelar(id, req.motivo(), usuario);
    }
}
