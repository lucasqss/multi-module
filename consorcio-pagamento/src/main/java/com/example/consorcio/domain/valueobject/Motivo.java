package com.example.consorcio.domain.valueobject;

import com.example.consorcio.domain.exception.RegraNegocioException;

/** Justificativa de rejeição/cancelamento. */
public record Motivo(String texto) {

    public static final int TAMANHO_MAXIMO = 255;

    public Motivo {
        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException("MOTIVO_OBRIGATORIO", "Motivo é obrigatório");
        }
        texto = texto.strip();
        if (texto.length() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("MOTIVO_MUITO_LONGO",
                    "Motivo deve ter no máximo %d caracteres".formatted(TAMANHO_MAXIMO));
        }
    }

    @Override
    public String toString() {
        return texto;
    }
}
