package com.example.boleto.domain.exception;

import java.time.LocalDate;

/** Pagamento informado com data futura. */
public class DataPagamentoFuturaException extends RegraNegocioException {

    public static final String CODIGO = "DATA_PAGAMENTO_FUTURA";

    public DataPagamentoFuturaException(LocalDate data) {
        super(CODIGO, "Data do pagamento %s não pode estar no futuro".formatted(data));
    }
}
