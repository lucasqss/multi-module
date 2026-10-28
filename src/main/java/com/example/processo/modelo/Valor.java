package com.example.processo.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valor monetário do processo, válido por construção: maior que zero e com no máximo 2 casas decimais. A escala é
 * sempre 2, então {@code 400000} e {@code 400000.00} são o mesmo valor (o Oracle devolve sem os zeros à direita).
 */
public record Valor(BigDecimal quantia) {

    public Valor {
        if (quantia == null || quantia.signum() <= 0) {
            throw new RegraVioladaException("Valor do processo deve ser maior que zero");
        }
        try {
            quantia = quantia.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new RegraVioladaException("Valor deve ter no máximo 2 casas decimais");
        }
    }

    public Valor(String quantia) {
        this(new BigDecimal(quantia));
    }

    @Override
    public String toString() {
        return quantia.toPlainString();
    }
}
