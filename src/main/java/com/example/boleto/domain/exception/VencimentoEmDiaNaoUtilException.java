package com.example.boleto.domain.exception;

import java.time.LocalDate;

/** Vencimento em fim de semana. */
public class VencimentoEmDiaNaoUtilException extends RegraNegocioException {

    public static final String CODIGO = "VENCIMENTO_EM_DIA_NAO_UTIL";

    public VencimentoEmDiaNaoUtilException(LocalDate vencimento) {
        super(CODIGO, "Data de vencimento %s cai em fim de semana".formatted(vencimento));
    }
}
