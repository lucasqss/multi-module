package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.PagamentoId;

/** Não existe pagamento com o id (ou com a transação bancária) informado. */
public class PagamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "PAGAMENTO_NAO_ENCONTRADO";

    public PagamentoNaoEncontradoException(PagamentoId id) {
        super(CODIGO, "Pagamento não encontrado: " + id);
    }

    public PagamentoNaoEncontradoException(String idTransacao) {
        super(CODIGO, "Nenhum pagamento com a transação " + idTransacao);
    }
}
