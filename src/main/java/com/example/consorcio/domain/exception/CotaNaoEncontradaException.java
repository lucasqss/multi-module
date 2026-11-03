package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.IdentificadorCota;

/** A cota não existe no sistema de cotas. */
public class CotaNaoEncontradaException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "COTA_NAO_ENCONTRADA";

    public CotaNaoEncontradaException(IdentificadorCota cota) {
        super(CODIGO, "Cota não encontrada: " + cota);
    }
}
