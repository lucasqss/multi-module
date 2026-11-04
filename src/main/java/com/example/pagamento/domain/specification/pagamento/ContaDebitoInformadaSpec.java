package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.ContaDebitoNaoInformadaException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: conta para débito informada. */
public class ContaDebitoInformadaSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return s.contaDebito() != null;
    }

    @Override
    protected ContaDebitoNaoInformadaException violacao(SubmissaoPagamento s) {
        return new ContaDebitoNaoInformadaException();
    }
}
