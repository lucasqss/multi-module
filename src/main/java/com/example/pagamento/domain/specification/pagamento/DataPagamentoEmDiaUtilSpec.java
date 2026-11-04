package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.DataPagamentoEmDiaNaoUtilException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Data de pagamento em dia útil (fake: só olha fim de semana). */
public class DataPagamentoEmDiaUtilSpec extends RegraNegocio<SubmissaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return s.data().map(d -> switch (d.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        }).orElse(true);
    }

    @Override
    protected DataPagamentoEmDiaNaoUtilException violacao(SubmissaoPagamento s) {
        return new DataPagamentoEmDiaNaoUtilException(s.dataPagamento());
    }
}
