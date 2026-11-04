package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.ContaDebitoNaoInformadaException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: conta para débito informada. */
public class ContaDebitoInformadaSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return s.contaDebito() != null;
    }

    @Override
    protected ContaDebitoNaoInformadaException violacao(SubmissaoProcesso s) {
        return new ContaDebitoNaoInformadaException();
    }
}
