package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.exception.ValorAcimaDoPercentualException;
import com.example.financiamento.domain.specification.RegraNegocio;
import com.example.financiamento.domain.valueobject.Dinheiro;

/** O valor solicitado não passa de um percentual do valor avaliado do bem. Parametrizada por tipo. */
public class PercentualFinanciadoSpec extends RegraNegocio<SubmissaoFinanciamento> {

    private final int percentual;

    public PercentualFinanciadoSpec(int percentual) {
        this.percentual = percentual;
    }

    private Dinheiro maximo(SubmissaoFinanciamento s) {
        return s.bem().valorAvaliado().percentual(percentual);
    }

    @Override
    public boolean isSatisfiedBy(SubmissaoFinanciamento s) {
        return !s.valorSolicitado().maiorQue(maximo(s));
    }

    @Override
    protected ValorAcimaDoPercentualException violacao(SubmissaoFinanciamento s) {
        return new ValorAcimaDoPercentualException(percentual, maximo(s));
    }
}
