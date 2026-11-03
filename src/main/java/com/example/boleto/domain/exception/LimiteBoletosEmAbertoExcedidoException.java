package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.Documento;

/** Pagador já atingiu o máximo de boletos em aberto. */
public class LimiteBoletosEmAbertoExcedidoException extends RegraNegocioException {

    public static final String CODIGO = "LIMITE_BOLETOS_EM_ABERTO_EXCEDIDO";

    public LimiteBoletosEmAbertoExcedidoException(Documento documento, int limite) {
        super(CODIGO, "Pagador %s já possui %d boletos em aberto".formatted(documento, limite));
    }
}
