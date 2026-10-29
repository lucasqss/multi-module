package br.com.consorcio.utilizacao.rest.parceiro.pagamentos;

import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.PagamentoApi;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/** O sistema de pagamentos chama aqui. Contrato estável: muda só com versão combinada com o parceiro. */
@Path("/parceiro/pagamentos/retornos")
public class RetornoPagamentoController {

    public record Retorno(String chaveIdempotencia, Long ordemId, String resultado) {
    }

    public record Recebido(Long ordemId, String situacao) {
    }

    @Inject
    PagamentoApi pagamentos;

    @POST
    public Recebido receber(Retorno req) {
        var o = pagamentos.receberRetorno(req.chaveIdempotencia(), req.ordemId(), req.resultado(), "sistema-pagamentos");
        return new Recebido(o.id(), o.situacao().name());
    }
}
