package com.example.pagamento.domain.exception;

import java.util.UUID;

/** Não existe pagamento com o id informado. */
public class PagamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "PAGAMENTO_NAO_ENCONTRADO";

    public PagamentoNaoEncontradoException(UUID id) {
        super(CODIGO, "Pagamento não encontrado: " + id);
    }
}
