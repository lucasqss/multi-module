package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.VencimentoAlemDoPrazoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Vencimento limitado a N dias a partir de hoje (N vem de configuração). */
public class VencimentoDentroDoPrazoSpec extends RegraNegocio<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return !s.dados().dataVencimento().isAfter(s.hoje().plusDays(s.prazoMaximoVencimentoDias()));
    }

    @Override
    protected VencimentoAlemDoPrazoException violacao(SolicitacaoBoleto s) {
        return new VencimentoAlemDoPrazoException(s.prazoMaximoVencimentoDias());
    }
}
