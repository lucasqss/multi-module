package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.Dinheiro;

/** Pagamento parcial ou a maior. */
public class ValorPagoDivergenteException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_PAGO_DIVERGENTE";

    public ValorPagoDivergenteException(Dinheiro valorPago, Dinheiro valorBoleto) {
        super(CODIGO, "Valor pago %s difere do valor do boleto %s".formatted(valorPago, valorBoleto));
    }
}
