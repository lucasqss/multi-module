package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;

/** Não existe processo de pagamento com o id informado. */
public class ProcessoPagamentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public static final String CODIGO = "PROCESSO_PAGAMENTO_NAO_ENCONTRADO";

    public ProcessoPagamentoNaoEncontradoException(ProcessoPagamentoId id) {
        super(CODIGO, "Processo de pagamento não encontrado: " + id);
    }
}
