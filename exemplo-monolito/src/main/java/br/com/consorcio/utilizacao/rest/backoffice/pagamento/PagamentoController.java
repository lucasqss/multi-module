package br.com.consorcio.utilizacao.rest.backoffice.pagamento;

import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.FinalidadeOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.PagamentoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.PagamentoApi.OrdemResumo;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.math.BigDecimal;
import java.util.List;

/** Jornada de pagamento: preparar, aprovar (quatro olhos) e acompanhar a ordem. */
@Path("/backoffice/pagamento/ordens")
public class PagamentoController {

    public record Origem(Long processoCotaId, Long recursoId, BigDecimal valor) {
    }

    public record NovaOrdem(Long processoId, Long beneficiarioParticipacaoId, Long contaRecebimentoId, String finalidade,
                            String meio, List<Origem> origens) {
    }

    public record Ordem(Long id, Long processoId, String situacao, BigDecimal valor, String idExterno) {

        static Ordem de(OrdemResumo o) {
            return new Ordem(o.id(), o.processoId(), o.situacao().name(), o.valor(), o.idExterno());
        }
    }

    @Inject
    PagamentoApi pagamentos;

    @POST
    public Ordem preparar(NovaOrdem req, @HeaderParam("X-Usuario") String usuario) {
        return Ordem.de(pagamentos.preparar(new PagamentoApi.PrepararOrdem(req.processoId(), req.beneficiarioParticipacaoId(),
                req.contaRecebimentoId(), FinalidadeOrdem.valueOf(req.finalidade()), req.meio(),
                req.origens().stream().map(o -> new PagamentoApi.Origem(o.processoCotaId(), o.recursoId(), o.valor())).toList()),
                usuario));
    }

    @POST
    @Path("/{id}/aprovacao")
    public Ordem aprovar(@PathParam("id") Long id, @HeaderParam("X-Usuario") String usuario) {
        return Ordem.de(pagamentos.aprovar(id, usuario));
    }

    @GET
    @Path("/{id}")
    public Ordem obter(@PathParam("id") Long id) {
        return Ordem.de(pagamentos.ordem(id));
    }
}
