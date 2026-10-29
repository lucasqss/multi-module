package br.com.consorcio.utilizacao.rest.backoffice.analise;

import br.com.consorcio.utilizacao.dominio.nucleo.documento.api.DocumentoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.math.BigDecimal;
import java.util.Base64;

/** Jornada de análise: documentos e laudo. */
@Path("/backoffice/analise")
public class AnaliseController {

    public record NovoDocumento(String tipoDocumento, String nomeArquivo, String mime, String conteudoBase64,
                                Long objetoId, Long participacaoId) {
    }

    public record Avaliacao(BigDecimal valorAvaliado) {
    }

    public record Criado(Long id) {
    }

    @Inject
    DocumentoApi documentos;

    @Inject
    ProcessoApi processos;

    @POST
    @Path("/{id}/documentos")
    public Criado enviarDocumento(@PathParam("id") Long id, NovoDocumento req) {
        return new Criado(documentos.enviar(new DocumentoApi.EnviarDocumento(id, req.tipoDocumento(), req.nomeArquivo(),
                req.mime(), Base64.getDecoder().decode(req.conteudoBase64()), req.objetoId(), req.participacaoId())));
    }

    @POST
    @Path("/{id}/objetos/{objeto}/avaliacao")
    public void informarAvaliacao(@PathParam("id") Long id, @PathParam("objeto") Long objeto, Avaliacao req,
                                  @HeaderParam("X-Usuario") String usuario) {
        processos.informarAvaliacao(id, objeto, req.valorAvaliado(), usuario);
    }
}
