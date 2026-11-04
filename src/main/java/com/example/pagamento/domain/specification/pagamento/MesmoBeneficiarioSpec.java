package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.BeneficiariosDiferentesException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Todos os boletos do mesmo beneficiário (é o que define um PROCESSO). */
public class MesmoBeneficiarioSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return s.boletos().mesmoBeneficiario();
    }

    @Override
    protected BeneficiariosDiferentesException violacao(SubmissaoPagamento s) {
        return new BeneficiariosDiferentesException();
    }
}
