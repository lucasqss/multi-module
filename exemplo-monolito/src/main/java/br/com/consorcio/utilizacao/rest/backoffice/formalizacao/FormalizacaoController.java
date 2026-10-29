package br.com.consorcio.utilizacao.rest.backoffice.formalizacao;

import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.api.FormalizacaoApi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.time.LocalDate;

/** Jornada de formalização: instrumento e constituição da garantia. */
@Path("/backoffice/formalizacao")
public class FormalizacaoController {

    public record Protocolo(String orgao, String protocolo) {
    }

    public record Registro(String numeroRegistro, LocalDate registradoEm) {
    }

    public record Criado(Long id) {
    }

    @Inject
    FormalizacaoMontador montador;

    @Inject
    FormalizacaoApi formalizacao;

    @Inject
    GarantiaApi garantias;

    @GET
    @Path("/{id}")
    public PainelFormalizacao painel(@PathParam("id") Long id) {
        return montador.painel(id);
    }

    @POST
    @Path("/{id}/instrumento")
    public Criado redigirInstrumento(@PathParam("id") Long id, @HeaderParam("X-Usuario") String usuario) {
        return new Criado(formalizacao.redigirCompraVendaComAlienacao(id, usuario).id());
    }

    @POST
    @Path("/garantias/{garantia}/protocolo")
    public void protocolar(@PathParam("garantia") Long garantia, Protocolo req, @HeaderParam("X-Usuario") String usuario) {
        garantias.registrarProtocolo(garantia, req.orgao(), req.protocolo(), usuario);
    }

    @POST
    @Path("/garantias/{garantia}/registro")
    public void registrar(@PathParam("garantia") Long garantia, Registro req, @HeaderParam("X-Usuario") String usuario) {
        garantias.registrarRegistro(garantia, req.numeroRegistro(), req.registradoEm(), usuario);
    }

    @POST
    @Path("/garantias/{garantia}/conferencia")
    public void conferir(@PathParam("garantia") Long garantia, @HeaderParam("X-Usuario") String usuario) {
        garantias.conferirRegistro(garantia, usuario);
    }
}
