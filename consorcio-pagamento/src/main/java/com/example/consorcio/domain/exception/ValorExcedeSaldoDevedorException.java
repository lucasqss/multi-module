package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.Dinheiro;

/** Pagamento maior que o saldo devedor da cota. */
public class ValorExcedeSaldoDevedorException extends RegraNegocioException {

    public static final String CODIGO = "VALOR_EXCEDE_SALDO_DEVEDOR";

    public ValorExcedeSaldoDevedorException(Dinheiro valor, Dinheiro saldoDevedor) {
        super(CODIGO, "Valor %s excede o saldo devedor da cota (%s)".formatted(valor, saldoDevedor));
    }
}
