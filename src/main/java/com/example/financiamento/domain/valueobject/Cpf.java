package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.CpfInvalidoException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** CPF do proponente: 11 dígitos, não todos iguais (validação simplificada, sem dígito verificador). */
public record Cpf(String valor) {

    public Cpf {
        valor = valor == null ? "" : valor.replaceAll("[.\\-\\s]", "");
        if (!valor.matches("\\d{11}") || valor.chars().distinct().count() == 1) {
            throw new CpfInvalidoException();
        }
    }

    @Override
    public String toString() {
        return valor;
    }

    /** Persistência: coluna texto. */
    @Converter(autoApply = true)
    public static class Conversor implements AttributeConverter<Cpf, String> {
        @Override public String convertToDatabaseColumn(Cpf v) { return v == null ? null : v.valor(); }
        @Override public Cpf convertToEntityAttribute(String v) { return v == null ? null : new Cpf(v); }
    }
}
