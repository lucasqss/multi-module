package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.DataPagamentoEmDiaNaoUtilException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Data de pagamento em dia útil (fake: só olha fim de semana). */
public class DataPagamentoEmDiaUtilSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return s.data().map(d -> switch (d.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        }).orElse(true);
    }

    @Override
    protected DataPagamentoEmDiaNaoUtilException violacao(SubmissaoProcesso s) {
        return new DataPagamentoEmDiaNaoUtilException(s.dataPagamento());
    }
}
