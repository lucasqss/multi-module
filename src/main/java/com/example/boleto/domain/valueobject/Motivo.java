package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.MotivoInvalidoException;

/** Justificativa de rejeição/cancelamento. */
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
}
