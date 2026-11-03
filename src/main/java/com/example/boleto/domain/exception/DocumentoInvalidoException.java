package com.example.boleto.domain.exception;

/** CPF/CNPJ com formato inválido. */
public class DocumentoInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "DOCUMENTO_INVALIDO";

    public DocumentoInvalidoException(String motivo) {
        super(CODIGO, "Documento inválido: " + motivo);
    }
}
