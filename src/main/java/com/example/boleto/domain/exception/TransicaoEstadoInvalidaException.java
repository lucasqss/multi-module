package com.example.boleto.domain.exception;

import com.example.boleto.domain.state.StatusBoleto;

/**
 * A ação não é permitida no status atual. Lançada pelos estados do State pattern.
 * Uma classe só basta aqui: status atual + ação já identificam o caso. Se precisar diferenciar
 * (ex.: tratar "pagar boleto cancelado" de forma especial), crie subclasses.
 */
public class TransicaoEstadoInvalidaException extends ConflitoException {

    public static final String CODIGO = "TRANSICAO_ESTADO_INVALIDA";

    private final StatusBoleto statusAtual;
    private final String acao;

    public TransicaoEstadoInvalidaException(StatusBoleto statusAtual, String acao) {
        super(CODIGO, "Não é possível %s um boleto com status %s".formatted(acao, statusAtual));
        this.statusAtual = statusAtual;
        this.acao = acao;
    }

    public StatusBoleto getStatusAtual() {
        return statusAtual;
    }

    public String getAcao() {
        return acao;
    }
}
