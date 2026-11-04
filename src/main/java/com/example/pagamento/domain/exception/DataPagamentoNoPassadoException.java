package com.example.pagamento.domain.exception;

import java.time.LocalDate;

/** Data de pagamento anterior a hoje. */
public class DataPagamentoNoPassadoException extends RegraNegocioException {

    public static final String CODIGO = "DATA_PAGAMENTO_NO_PASSADO";

    public DataPagamentoNoPassadoException(LocalDate data) {
        super(CODIGO, "Data de pagamento %s não pode estar no passado".formatted(data));
    }
}
