package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** A ação pedida não é permitida no estado atual do processo (lançada pelos estados do State pattern). */
public class TransicaoEstadoInvalidaException extends DomainException {

    public TransicaoEstadoInvalidaException(StatusProcessoPagamento statusAtual, String acao) {
        super("TRANSICAO_INVALIDA",
                "Não é possível %s um processo com status %s".formatted(acao, statusAtual));
    }
}
