package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.Dinheiro;

/** Boleto abaixo do valor mínimo. */
public class ValorAbaixoDoMinimoException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_ABAIXO_DO_MINIMO";

    public ValorAbaixoDoMinimoException(Dinheiro minimo) {
        super(CODIGO, "Valor do boleto deve ser de no mínimo " + minimo);
    }
}
