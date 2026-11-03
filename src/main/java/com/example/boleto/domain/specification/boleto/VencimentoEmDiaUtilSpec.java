package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.VencimentoEmDiaNaoUtilException;
import com.example.boleto.domain.specification.RegraNegocio;

/**
 * Vencimento precisa cair em dia útil.
 * (Regra fake simplificada: só olha fim de semana. Feriados viriam de um calendário, entregue na solicitação.)
 */
public class VencimentoEmDiaUtilSpec extends RegraNegocio<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return switch (s.dataVencimento().getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }

    @Override
    protected VencimentoEmDiaNaoUtilException violacao(EmissaoBoleto s) {
        return new VencimentoEmDiaNaoUtilException(s.dataVencimento());
    }
}
