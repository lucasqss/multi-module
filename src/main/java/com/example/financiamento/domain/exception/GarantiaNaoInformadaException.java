package com.example.financiamento.domain.exception;

/** Submissão sem o bem dado em garantia. */
public class GarantiaNaoInformadaException extends RegraNegocioException {

    public static final String CODIGO = "GARANTIA_NAO_INFORMADA";

    public GarantiaNaoInformadaException() {
        super(CODIGO, "Informe o bem dado em garantia");
    }
}
