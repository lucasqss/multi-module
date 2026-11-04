package com.example.financiamento.domain.exception;

import java.util.UUID;

/** Não existe financiamento com o id informado. */
public class FinanciamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "FINANCIAMENTO_NAO_ENCONTRADO";

    public FinanciamentoNaoEncontradoException(UUID id) {
        super(CODIGO, "Financiamento não encontrado: " + id);
    }
}
