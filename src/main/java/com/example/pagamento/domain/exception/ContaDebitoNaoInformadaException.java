package com.example.pagamento.domain.exception;

/** Completude: submissão sem conta para débito. */
public class ContaDebitoNaoInformadaException extends RegraNegocioException {

    public static final String CODIGO = "CONTA_DEBITO_NAO_INFORMADA";

    public ContaDebitoNaoInformadaException() {
        super(CODIGO, "Informe a conta para débito");
    }
}
