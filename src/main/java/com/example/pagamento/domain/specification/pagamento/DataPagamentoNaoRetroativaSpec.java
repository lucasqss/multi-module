package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Data de pagamento a partir de hoje. */
public class DataPagamentoNaoRetroativaSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return s.data().map(d -> !d.isBefore(s.hoje())).orElse(true);
    }

    @Override
    protected DataPagamentoNoPassadoException violacao(SubmissaoPagamento s) {
        return new DataPagamentoNoPassadoException(s.dataPagamento());
    }
}
