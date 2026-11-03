package com.example.boleto.domain.exception;

/** Nome ou e-mail do pagador inválido. */
public class PagadorInvalidoException extends DadoInvalidoException {

    public static final String CODIGO = "PAGADOR_INVALIDO";

    public PagadorInvalidoException(String motivo) {
        super(CODIGO, "Pagador inválido: " + motivo);
    }
}
