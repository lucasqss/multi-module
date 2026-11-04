package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.MotivoInvalidoException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Justificativa (ex.: de um cancelamento). */
public record Motivo(String texto) {

    public static final int TAMANHO_MAXIMO = 255;

    public Motivo {
        if (texto == null || texto.isBlank()) {
            throw new MotivoInvalidoException("é obrigatório");
        }
        texto = texto.strip();
        if (texto.length() > TAMANHO_MAXIMO) {
            throw new MotivoInvalidoException("deve ter no máximo %d caracteres".formatted(TAMANHO_MAXIMO));
        }
    }

    @Override
    public String toString() {
        return texto;
    }

    /** Persistência: coluna texto. {@code autoApply}: vale para todo atributo {@code Motivo}. */
    @Converter(autoApply = true)
    public static class Conversor implements AttributeConverter<Motivo, String> {
        @Override public String convertToDatabaseColumn(Motivo v) { return v == null ? null : v.texto(); }
        @Override public Motivo convertToEntityAttribute(String v) { return v == null ? null : new Motivo(v); }
    }
}
