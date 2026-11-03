package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.Dinheiro;

/** Boleto acima do valor máximo. */
public class ValorAcimaDoMaximoException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_ACIMA_DO_MAXIMO";

    public ValorAcimaDoMaximoException(Dinheiro maximo) {
        super(CODIGO, "Valor do boleto deve ser de no máximo " + maximo);
    }
}
