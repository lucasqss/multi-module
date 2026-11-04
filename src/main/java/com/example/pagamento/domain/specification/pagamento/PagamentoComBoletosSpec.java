package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.PagamentoSemBoletosException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: ao menos um boleto. */
public class PagamentoComBoletosSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return !s.boletos().vazio();
    }

    @Override
    protected PagamentoSemBoletosException violacao(SubmissaoPagamento s) {
        return new PagamentoSemBoletosException();
    }
}
