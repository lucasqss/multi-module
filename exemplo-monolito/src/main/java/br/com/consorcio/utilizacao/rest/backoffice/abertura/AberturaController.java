package br.com.consorcio.utilizacao.rest.backoffice.abertura;

import br.com.consorcio.utilizacao.dominio.modalidade.imovel.api.ImovelApi;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.Papel;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.PartesApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import br.com.consorcio.utilizacao.rest.backoffice.abertura.AberturaDto.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

/** Jornada de abertura no backoffice. Sem composição: mapeia direto para as APIs, sem montador. */
@Path("/backoffice/abertura")
public class AberturaController {

    @Inject
    ImovelApi imovel;

    @Inject
    ProcessoApi processos;

    @Inject
    PartesApi partes;

    @POST
    @Path("/imoveis")
    public Criado abrirImovel(NovoProcessoImovel req, @HeaderParam("X-Usuario") String usuario) {
        ContatoAcesso c = req.contatoAcesso();
        return new Criado(imovel.abrir(new ImovelApi.AbrirProcessoImovel(req.cpfTitular(), req.nomeTitular(),
                req.tipoOperacao(), req.matricula(), req.cartorioRi(), req.inscricaoIptu(), req.uf(), req.municipio(),
                req.construcaoAverbada(), req.valorNegociado(),
                c == null ? null : c.nome(), c == null ? null : c.email(), c == null ? null : c.telefone()), usuario));
    }

    @POST
    @Path("/{id}/participantes")
    public Criado adicionarParticipante(@PathParam("id") Long id, NovoParticipante req) {
        return new Criado(processos.adicionarParticipante(id, req.tipo(), req.cpfCnpj(), req.nome(), Papel.valueOf(req.papel())));
    }

    @POST
    @Path("/{id}/participantes/{participacao}/contas")
    public Criado adicionarConta(@PathParam("id") Long id, @PathParam("participacao") Long participacao, NovaConta req) {
        processos.exigirPertenca(id, null, participacao);
        return new Criado(partes.adicionarConta(participacao,
                new PartesApi.NovaConta(req.banco(), req.agencia(), req.conta(), req.chavePix(), req.titularidadeVerificada())));
    }

    @POST
    @Path("/{id}/cotas")
    public CotaReservada reservarCota(@PathParam("id") Long id, NovaReserva req, @HeaderParam("X-Usuario") String usuario) {
        var c = processos.reservarCota(id, req.cotaRef(), req.valor(), usuario);
        return new CotaReservada(c.id(), c.cotaRef(), c.situacao(), c.reservado());
    }

    @POST
    @Path("/{id}/comunicacao-formal")
    public void confirmarComunicacaoFormal(@PathParam("id") Long id, @HeaderParam("X-Usuario") String usuario) {
        processos.confirmarComunicacaoFormal(id, usuario);
    }
}
