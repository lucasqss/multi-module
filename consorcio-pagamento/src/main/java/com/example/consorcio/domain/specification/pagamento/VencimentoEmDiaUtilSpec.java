package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.VencimentoEmDiaNaoUtilException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/**
 * Vencimento precisa cair em dia útil.
 * (Regra fake simplificada: só olha fim de semana. Feriados viriam de uma porta para um calendário.)
 */
public class VencimentoEmDiaUtilSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return switch (s.dados().dataVencimento().getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }

    @Override
    protected VencimentoEmDiaNaoUtilException violacao(SolicitacaoPagamento s) {
        return new VencimentoEmDiaNaoUtilException(s.dados().dataVencimento());
    }
}
