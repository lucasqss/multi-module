package com.example.boleto.domain.exception;

/** Seção de destinatários com e-mail repetido. */
public class DestinatarioRepetidoException extends RegraNegocioException {

    public static final String CODIGO = "DESTINATARIO_REPETIDO";

    public DestinatarioRepetidoException() {
        super(CODIGO, "Há e-mails repetidos entre os destinatários (ou iguais ao do pagador)");
    }
}
