package com.example.pagamento.domain.exception;

import com.example.pagamento.domain.state.StatusProcesso;

/**
 * A ação não é permitida no status atual. Lançada pelos estados do State pattern.
 * Status atual + ação já identificam o caso; se precisar diferenciar, crie subclasses.
 */
public class TransicaoEstadoInvalidaException extends ConflitoException {

    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";

    private final StatusProcesso statusAtual;
    private final String acao;

    public TransicaoEstadoInvalidaException(StatusProcesso statusAtual, String acao) {
        super(CODIGO, "Não é possível %s um processo com status %s".formatted(acao, statusAtual));
        this.statusAtual = statusAtual;
        this.acao = acao;
    }

    public StatusProcesso getStatusAtual() {
        return statusAtual;
    }

    public String getAcao() {
        return acao;
    }
}
