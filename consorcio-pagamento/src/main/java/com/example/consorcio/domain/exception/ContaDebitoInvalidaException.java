package com.example.consorcio.domain.exception;

/** Agência/conta com formato inválido. */
public class ContaDebitoInvalidaException extends DadoInvalidoException {

    public static final String CODIGO = "CONTA_DEBITO_INVALIDA";

    public ContaDebitoInvalidaException(String motivo) {
        super(CODIGO, "Conta para débito inválida: " + motivo);
    }
}
