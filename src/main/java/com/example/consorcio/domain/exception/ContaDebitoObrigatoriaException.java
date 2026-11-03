package com.example.consorcio.domain.exception;

/** Débito em conta sem conta informada. */
public class ContaDebitoObrigatoriaException extends RegraNegocioException {

    public static final String CODIGO = "CONTA_DEBITO_OBRIGATORIA";

    public ContaDebitoObrigatoriaException() {
        super(CODIGO, "Conta para débito é obrigatória para débito em conta");
    }
}
