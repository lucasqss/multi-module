package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.Dinheiro;

/** PIX acima do limite. */
public class LimitePixExcedidoException extends RegraNegocioException {

    public static final String CODIGO = "LIMITE_PIX_EXCEDIDO";

    public LimitePixExcedidoException(Dinheiro limite) {
        super(CODIGO, "Pagamento via PIX limitado a " + limite);
    }
}
