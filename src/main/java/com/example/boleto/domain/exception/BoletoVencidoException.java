package com.example.boleto.domain.exception;

import java.time.LocalDate;

/** Pagamento após o vencimento (regra fake: não há cálculo de juros/multa). */
public class BoletoVencidoException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_VENCIDO";

    public BoletoVencidoException(LocalDate vencimento, LocalDate dataPagamento) {
        super(CODIGO, "Boleto vencido em %s não aceita pagamento em %s".formatted(vencimento, dataPagamento));
    }
}
