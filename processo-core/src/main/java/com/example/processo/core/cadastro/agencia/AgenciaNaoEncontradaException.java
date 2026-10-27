package com.example.processo.core.cadastro.agencia;

public class AgenciaNaoEncontradaException extends RuntimeException {

    public AgenciaNaoEncontradaException(String codigo) {
        super("Agência " + codigo + " não encontrada");
    }
}
