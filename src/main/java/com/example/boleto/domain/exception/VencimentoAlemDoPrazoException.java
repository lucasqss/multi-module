package com.example.boleto.domain.exception;

/** Vencimento além do prazo máximo configurado. */
public class VencimentoAlemDoPrazoException extends RegraNegocioException {

    public static final String CODIGO = "VENCIMENTO_ALEM_DO_PRAZO";

    public VencimentoAlemDoPrazoException(int prazoMaximoDias) {
        super(CODIGO, "Data de vencimento deve ser em até %d dias".formatted(prazoMaximoDias));
    }
}
