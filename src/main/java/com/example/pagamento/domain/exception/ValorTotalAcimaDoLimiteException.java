package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.Dinheiro;

/** Modalidade LOTE: soma dos boletos acima do limite por lote. */
public class ValorTotalAcimaDoLimiteException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_TOTAL_ACIMA_DO_LIMITE";

    public ValorTotalAcimaDoLimiteException(Dinheiro limite) {
        super(CODIGO, "Valor total acima do limite de " + limite);
    }
}
