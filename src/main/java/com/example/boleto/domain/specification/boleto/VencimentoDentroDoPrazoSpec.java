package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.VencimentoAlemDoPrazoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Vencimento limitado a N dias a partir de hoje (N vem de configuração). */
public class VencimentoDentroDoPrazoSpec extends RegraNegocio<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return !s.dataVencimento().isAfter(s.hoje().plusDays(s.condicoes().prazoMaximoVencimentoDias()));
    }

    @Override
    protected VencimentoAlemDoPrazoException violacao(EmissaoBoleto s) {
        return new VencimentoAlemDoPrazoException(s.condicoes().prazoMaximoVencimentoDias());
    }
}
