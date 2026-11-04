package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.ValorMonetarioInvalidoException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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

    /** {@code percentual}% deste valor, arredondado ao centavo. */
    public Dinheiro percentual(int percentual) {
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(percentual)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    /** Divide em {@code partes} iguais, arredondado ao centavo. */
    public Dinheiro dividido(int partes) {
        return new Dinheiro(valor.divide(BigDecimal.valueOf(partes), 2, RoundingMode.HALF_UP));
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return "R$ " + valor.toPlainString();
    }

    /** Persistência: coluna decimal. {@code autoApply}: vale para todo atributo {@code Dinheiro}. */
    @Converter(autoApply = true)
    public static class Conversor implements AttributeConverter<Dinheiro, BigDecimal> {
        @Override public BigDecimal convertToDatabaseColumn(Dinheiro d) { return d == null ? null : d.valor(); }
        @Override public Dinheiro convertToEntityAttribute(BigDecimal v) { return v == null ? null : new Dinheiro(v); }
    }
}
