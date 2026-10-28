package com.example.processo.modelo;

/** Uma regra de negócio impediu a operação; o motivo é apresentável ao usuário. */
public class RegraVioladaException extends RuntimeException {

    public RegraVioladaException(String motivo) {
        super(motivo);
    }
}
