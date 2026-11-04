package com.example.pagamento.domain.exception;

/** Seção de boletos com o mesmo boleto mais de uma vez. */
public class BoletoRepetidoNoPagamentoException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_REPETIDO_NO_PAGAMENTO";

    public BoletoRepetidoNoPagamentoException() {
        super(CODIGO, "O mesmo boleto aparece mais de uma vez no pagamento");
    }
}
