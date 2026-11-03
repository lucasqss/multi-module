package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.Dinheiro;

/** Pagador pessoa física (CPF) com valor acima do limite. */
public class LimitePessoaFisicaExcedidoException extends RegraNegocioException {

    public static final String CODIGO = "LIMITE_PESSOA_FISICA_EXCEDIDO";

    public LimitePessoaFisicaExcedidoException(Dinheiro limite) {
        super(CODIGO, "Boleto para pessoa física limitado a " + limite);
    }
}
