package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Cota cancelada/encerrada não recebe pagamento. */
public class CotaAceitaPagamentoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.cota().aceitaPagamento();
    }

    @Override
    public String codigo() {
        return "COTA_NAO_ACEITA_PAGAMENTO";
    }

    @Override
    public String mensagem(SolicitacaoPagamento s) {
        return "Cota %s está %s e não aceita pagamentos".formatted(s.cota().identificador(), s.cota().situacao());
    }
}
