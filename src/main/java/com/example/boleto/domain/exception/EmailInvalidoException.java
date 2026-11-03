package com.example.boleto.domain.exception;

/** E-mail com formato inválido. */
public class EmailInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "EMAIL_INVALIDO";

    public EmailInvalidoException(String email) {
        super(CODIGO, "E-mail inválido: " + email);
    }
}
