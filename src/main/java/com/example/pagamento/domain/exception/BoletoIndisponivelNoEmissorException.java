package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.LinhaDigitavel;

/**
 * O sistema emissor não encontrou o boleto (inexistente, cancelado ou já pago). Lançada pelo adapter do
 * emissor.
 */
public class BoletoIndisponivelNoEmissorException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_INDISPONIVEL_NO_EMISSOR";

    public BoletoIndisponivelNoEmissorException(LinhaDigitavel linha) {
        super(CODIGO, "O emissor não tem boleto em aberto com a linha digitável " + linha);
    }
}
