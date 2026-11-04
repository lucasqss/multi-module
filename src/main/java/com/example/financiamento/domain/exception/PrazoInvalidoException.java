package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.valueobject.TipoFinanciamento;

/** Prazo fora da faixa do tipo de financiamento. Parametrizada: uma classe serve aos dois tipos. */
public class PrazoInvalidoException extends RegraNegocioException {

    public static final String CODIGO = "PRAZO_INVALIDO";

    public PrazoInvalidoException(TipoFinanciamento tipo, int minimo, int maximo) {
        super(CODIGO, "Financiamento de %s aceita prazo de %d a %d meses".formatted(tipo, minimo, maximo));
    }
}
