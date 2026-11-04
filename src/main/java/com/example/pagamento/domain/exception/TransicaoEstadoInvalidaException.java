package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.state.StatusPagamento;

/**
 * A ação não é permitida no status atual. Lançada pelos estados do State pattern.
 * Status atual + ação já identificam o caso; se precisar diferenciar, crie subclasses.
 */
public class TransicaoEstadoInvalidaException extends ConflitoException {

    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";

    private final StatusPagamento statusAtual;
    private final String acao;

    public TransicaoEstadoInvalidaException(StatusPagamento statusAtual, String acao) {
        super(CODIGO, "Não é possível %s um pagamento com status %s".formatted(acao, statusAtual));
        this.statusAtual = statusAtual;
        this.acao = acao;
    }

    public StatusPagamento getStatusAtual() {
        return statusAtual;
    }

    public String getAcao() {
        return acao;
    }
}
