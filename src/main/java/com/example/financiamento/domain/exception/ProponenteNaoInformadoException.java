package com.example.financiamento.domain.exception;

/** Submissão sem os dados do proponente. */
public class ProponenteNaoInformadoException extends RegraNegocioException {

    public static final String CODIGO = "PROPONENTE_NAO_INFORMADO";

    public ProponenteNaoInformadoException() {
        super(CODIGO, "Informe os dados do proponente");
    }
}
