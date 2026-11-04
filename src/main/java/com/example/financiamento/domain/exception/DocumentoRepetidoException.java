package com.example.financiamento.domain.exception;

/** Seção de documentos com o mesmo tipo de documento mais de uma vez. */
public class DocumentoRepetidoException extends RegraNegocioException {

    public static final String CODIGO = "DOCUMENTO_REPETIDO";

    public DocumentoRepetidoException() {
        super(CODIGO, "O mesmo tipo de documento aparece mais de uma vez");
    }
}
