package com.example.processo.modelo;

/** O estado atual não aceita o evento pedido. O motivo é apresentável ao usuário. */
public class TransicaoInvalidaException extends RuntimeException {

    TransicaoInvalidaException(Situacao atual, String acao) {
        super("Processo " + atual + " não pode ser " + acao);
    }
}
