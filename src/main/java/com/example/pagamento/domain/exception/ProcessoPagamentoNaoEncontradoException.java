package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;

/** Não existe processo com o id (ou com a transação bancária) informado. */
public class ProcessoPagamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "PROCESSO_PAGAMENTO_NAO_ENCONTRADO";

    public ProcessoPagamentoNaoEncontradoException(ProcessoPagamentoId id) {
        super(CODIGO, "Processo de pagamento não encontrado: " + id);
    }

    public ProcessoPagamentoNaoEncontradoException(String idTransacao) {
        super(CODIGO, "Nenhum processo de pagamento com a transação " + idTransacao);
    }
}
