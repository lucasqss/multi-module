package com.example.financiamento.domain.exception;

import com.example.financiamento.domain.state.StatusFinanciamento;

/**
 * A ação não é permitida no status atual. Lançada pelos estados do State pattern.
 * Status atual + ação já identificam o caso; se precisar diferenciar, crie subclasses.
 */
public class TransicaoEstadoInvalidaException extends ConflitoException {

    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";

    private final StatusFinanciamento statusAtual;
    private final String acao;

    public TransicaoEstadoInvalidaException(StatusFinanciamento statusAtual, String acao) {
        super(CODIGO, "Não é possível %s um financiamento com status %s".formatted(acao, statusAtual));
        this.statusAtual = statusAtual;
        this.acao = acao;
    }

    public StatusFinanciamento getStatusAtual() {
        return statusAtual;
    }

    public String getAcao() {
        return acao;
    }
}
