package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.valueobject.Dinheiro;

/** Valor solicitado acima do percentual financiável do bem. */
public class ValorAcimaDoPercentualException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_ACIMA_DO_PERCENTUAL";

    public ValorAcimaDoPercentualException(int percentual, Dinheiro maximo) {
        super(CODIGO, "Pode financiar até %d%% do valor do bem (%s)".formatted(percentual, maximo));
    }
}
