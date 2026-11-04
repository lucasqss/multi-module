package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.DataPagamentoNaoInformadaException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: data de pagamento informada. */
public class DataPagamentoInformadaSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return s.data().isPresent();
    }

    @Override
    protected DataPagamentoNaoInformadaException violacao(SubmissaoPagamento s) {
        return new DataPagamentoNaoInformadaException();
    }
}
