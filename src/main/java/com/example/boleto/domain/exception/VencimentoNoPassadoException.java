package com.example.boleto.domain.exception;

import java.time.LocalDate;

/** Vencimento anterior a hoje. */
public class VencimentoNoPassadoException extends RegraNegocioException {

    public static final String CODIGO = "VENCIMENTO_NO_PASSADO";

    public VencimentoNoPassadoException(LocalDate vencimento) {
        super(CODIGO, "Data de vencimento %s não pode estar no passado".formatted(vencimento));
    }
}
