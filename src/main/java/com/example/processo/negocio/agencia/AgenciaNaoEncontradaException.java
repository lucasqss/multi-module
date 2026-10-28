package com.example.processo.negocio.agencia;

public class AgenciaNaoEncontradaException extends RuntimeException {

    public AgenciaNaoEncontradaException(String codigo) {
        super("Agência " + codigo + " não encontrada");
    }
}
