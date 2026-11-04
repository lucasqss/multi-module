package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.LinhaDigitavelInvalidaException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

/**
 * Linha digitável de um boleto (47 dígitos). Quem GERA é o sistema emissor (externo); nós só a
 * recebemos (do usuário ou do emissor) e validamos o formato.
 */
public record LinhaDigitavel(String valor) {

    public LinhaDigitavel {
        Objects.requireNonNull(valor);
        valor = valor.replaceAll("[\\s.]", "");
        if (!valor.matches("\\d{47}")) {
            throw new LinhaDigitavelInvalidaException();
        }
    }

    @Override
    public String toString() {
        return valor;
    }

    /** Persistência: coluna texto. {@code autoApply}: vale para todo atributo {@code LinhaDigitavel}. */
    @Converter(autoApply = true)
    public static class Conversor implements AttributeConverter<LinhaDigitavel, String> {
        @Override public String convertToDatabaseColumn(LinhaDigitavel v) { return v == null ? null : v.valor(); }
        @Override public LinhaDigitavel convertToEntityAttribute(String v) { return v == null ? null : new LinhaDigitavel(v); }
    }
}
