package com.example.consorcio.domain.exception;

/** Conta informada para forma de pagamento que não é débito. */
public class ContaDebitoIndevidaException extends RegraNegocioException {

    public static final String CODIGO = "CONTA_DEBITO_INDEVIDA";

    public ContaDebitoIndevidaException() {
        super(CODIGO, "Conta para débito só deve ser informada para débito em conta");
    }
}
