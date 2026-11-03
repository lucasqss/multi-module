package com.example.consorcio.domain.valueobject;

import com.example.consorcio.domain.exception.IdentificadorCotaInvalidoException;

/** Identifica uma cota dentro de um grupo de consórcio (grupo + número da cota). */
public record IdentificadorCota(int grupo, int cota) {

    public IdentificadorCota {
        if (grupo < 1 || grupo > 999_999) {
            throw new IdentificadorCotaInvalidoException(grupo, cota);
        }
        if (cota < 1 || cota > 9_999) {
            throw new IdentificadorCotaInvalidoException(grupo, cota);
        }
    }

    @Override
    public String toString() {
        return "%06d-%04d".formatted(grupo, cota);
    }
}
