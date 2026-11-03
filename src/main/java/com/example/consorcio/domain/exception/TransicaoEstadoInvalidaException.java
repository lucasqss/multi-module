package com.example.consorcio.domain.exception;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/**
 * A ação não é permitida no status atual. Lançada pelos estados do State pattern.
 * Uma classe só basta aqui: status atual + ação já identificam o caso. Se precisar diferenciar
 * (ex.: tratar "confirmar processo cancelado" de forma especial), crie subclasses.
 */
public class TransicaoEstadoInvalidaException extends ConflitoException {

    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";

    private final StatusProcessoPagamento statusAtual;
    private final String acao;

    public TransicaoEstadoInvalidaException(StatusProcessoPagamento statusAtual, String acao) {
        super(CODIGO, "Não é possível %s um processo com status %s".formatted(acao, statusAtual));
        this.statusAtual = statusAtual;
        this.acao = acao;
    }

    public StatusProcessoPagamento getStatusAtual() {
        return statusAtual;
    }

    public String getAcao() {
        return acao;
    }
}
