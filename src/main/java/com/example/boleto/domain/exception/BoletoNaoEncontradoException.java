package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.BoletoId;

/** Não existe boleto com o id informado. */
public class BoletoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "BOLETO_NAO_ENCONTRADO";

    public BoletoNaoEncontradoException(BoletoId id) {
        super(CODIGO, "Boleto não encontrado: " + id);
    }
}
