package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.DataPagamentoNaoInformadaException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: data de pagamento informada. */
public class DataPagamentoInformadaSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return s.data().isPresent();
    }

    @Override
    protected DataPagamentoNaoInformadaException violacao(SubmissaoProcesso s) {
        return new DataPagamentoNaoInformadaException();
    }
}
