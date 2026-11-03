package com.example.consorcio.domain.valueobject;

import com.example.consorcio.domain.exception.RegraNegocioException;

/** Identifica uma cota dentro de um grupo de consórcio (grupo + número da cota). */
public record IdentificadorCota(int grupo, int cota) {

    public IdentificadorCota {
        if (grupo < 1 || grupo > 999_999) {
            throw new RegraNegocioException("GRUPO_INVALIDO", "Grupo deve estar entre 1 e 999999");
        }
        if (cota < 1 || cota > 9_999) {
            throw new RegraNegocioException("COTA_INVALIDA", "Cota deve estar entre 1 e 9999");
        }
    }

    @Override
    public String toString() {
        return "%06d-%04d".formatted(grupo, cota);
    }
}
