package com.example.consorcio.domain.exception;

/** Grupo ou número de cota fora da faixa. */
public class IdentificadorCotaInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "IDENTIFICADOR_COTA_INVALIDO";

    public IdentificadorCotaInvalidoException(int grupo, int cota) {
        super(CODIGO, "Grupo/cota inválido: %d/%d".formatted(grupo, cota));
    }
}
