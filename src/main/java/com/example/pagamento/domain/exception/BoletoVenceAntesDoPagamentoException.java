package com.example.pagamento.domain.exception;

import java.time.LocalDate;

/** Algum boleto vence antes da data agendada (regra fake: sem juros/multa). */
public class BoletoVenceAntesDoPagamentoException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_VENCE_ANTES_DO_PAGAMENTO";

    public BoletoVenceAntesDoPagamentoException(String beneficiario, LocalDate vencimento) {
        super(CODIGO, "Boleto de %s vence em %s, antes da data de pagamento".formatted(beneficiario, vencimento));
    }
}
