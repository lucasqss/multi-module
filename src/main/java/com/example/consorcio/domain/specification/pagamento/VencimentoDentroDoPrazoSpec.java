package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.VencimentoAlemDoPrazoException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Vencimento limitado a N dias a partir de hoje (N vem de configuração). */
public class VencimentoDentroDoPrazoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.dados().dataVencimento().isAfter(s.hoje().plusDays(s.prazoMaximoVencimentoDias()));
    }

    @Override
    protected VencimentoAlemDoPrazoException violacao(SolicitacaoPagamento s) {
        return new VencimentoAlemDoPrazoException(s.prazoMaximoVencimentoDias());
    }
}
