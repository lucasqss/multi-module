package com.example.boleto.domain.exception;

/** Linha digitável fora do formato (47 dígitos). */
public class LinhaDigitavelInvalidaException extends DadoInvalidoException {

    public static final String CODIGO = "LINHA_DIGITAVEL_INVALIDA";

    public LinhaDigitavelInvalidaException() {
        super(CODIGO, "Linha digitável deve ter 47 dígitos");
    }
}
