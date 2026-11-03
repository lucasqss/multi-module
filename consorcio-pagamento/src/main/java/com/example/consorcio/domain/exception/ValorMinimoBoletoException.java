package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.Dinheiro;

/** Boleto abaixo do valor mínimo. */
public class ValorMinimoBoletoException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_MINIMO_BOLETO";

    public ValorMinimoBoletoException(Dinheiro minimo) {
        super(CODIGO, "Boleto exige valor mínimo de " + minimo);
    }
}
