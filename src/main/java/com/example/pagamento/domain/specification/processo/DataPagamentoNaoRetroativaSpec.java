package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.DataPagamentoNoPassadoException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Data de pagamento a partir de hoje. */
public class DataPagamentoNaoRetroativaSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return s.data().map(d -> !d.isBefore(s.hoje())).orElse(true);
    }

    @Override
    protected DataPagamentoNoPassadoException violacao(SubmissaoProcesso s) {
        return new DataPagamentoNoPassadoException(s.dataPagamento());
    }
}
