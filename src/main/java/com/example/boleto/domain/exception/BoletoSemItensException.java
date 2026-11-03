package com.example.boleto.domain.exception;

/** Completude: tentativa de emitir um boleto sem itens. */
public class BoletoSemItensException extends RegraNegocioException {

    public static final String CODIGO = "BOLETO_SEM_ITENS";

    public BoletoSemItensException() {
        super(CODIGO, "O boleto precisa ter ao menos um item para ser emitido");
    }
}
