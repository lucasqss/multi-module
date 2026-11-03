package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.ValorMonetarioInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object imutável que encapsula valores monetários em reais.
 * Garante sozinho as próprias regras: não nulo, não negativo, no máximo 2 casas decimais.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor é obrigatório");
        if (valor.signum() < 0) {
            throw new ValorMonetarioInvalidoException("não pode ser negativo");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ValorMonetarioInvalidoException("aceita no máximo 2 casas decimais");
        }
        valor = valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public boolean ehPositivo() {
        return valor.signum() > 0;
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString();
    }
}
