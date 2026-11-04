package com.example.pagamento.domain.exception;

/** Completude: submissão sem data de pagamento. */
public class DataPagamentoNaoInformadaException extends RegraNegocioException {

    public static final String CODIGO = "DATA_PAGAMENTO_NAO_INFORMADA";

    public DataPagamentoNaoInformadaException() {
        super(CODIGO, "Informe a data de pagamento");
    }
}
