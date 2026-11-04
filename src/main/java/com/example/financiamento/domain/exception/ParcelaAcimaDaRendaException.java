package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.valueobject.Dinheiro;

/** A parcela (simplificada: valor / prazo, sem juros) passa do percentual da renda permitido. */
public class ParcelaAcimaDaRendaException extends RegraNegocioException {

    public static final String CODIGO = "PARCELA_ACIMA_DA_RENDA";

    public ParcelaAcimaDaRendaException(Dinheiro parcela, int percentualDaRenda) {
        super(CODIGO, "A parcela de %s passa de %d%% da renda".formatted(parcela, percentualDaRenda));
    }
}
