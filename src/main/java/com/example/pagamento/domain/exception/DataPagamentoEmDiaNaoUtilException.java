package com.example.pagamento.domain.exception;

import java.time.LocalDate;

/** Data de pagamento em fim de semana (regra fake: feriados ficam de fora). */
public class DataPagamentoEmDiaNaoUtilException extends RegraNegocioException {

    public static final String CODIGO = "DATA_PAGAMENTO_EM_DIA_NAO_UTIL";

    public DataPagamentoEmDiaNaoUtilException(LocalDate data) {
        super(CODIGO, "Data de pagamento %s cai em fim de semana".formatted(data));
    }
}
