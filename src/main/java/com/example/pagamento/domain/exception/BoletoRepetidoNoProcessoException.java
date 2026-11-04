package com.example.pagamento.domain.exception;

/** Seção de boletos com boleto repetido. */
public class BoletoRepetidoNoProcessoException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_REPETIDO_NO_PROCESSO";

    public BoletoRepetidoNoProcessoException() {
        super(CODIGO, "O mesmo boleto aparece mais de uma vez no processo");
    }
}
