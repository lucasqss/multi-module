package com.example.boleto.domain.exception;

import com.example.boleto.domain.valueobject.BoletoId;
import com.example.boleto.domain.valueobject.LinhaDigitavel;

/** Não existe boleto com o id (ou a linha digitável) informado. */
public class BoletoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "BOLETO_NAO_ENCONTRADO";

    public BoletoNaoEncontradoException(BoletoId id) {
        super(CODIGO, "Boleto não encontrado: " + id);
    }

    public BoletoNaoEncontradoException(LinhaDigitavel linhaDigitavel) {
        super(CODIGO, "Nenhum boleto emitido com a linha digitável " + linhaDigitavel);
    }
}
