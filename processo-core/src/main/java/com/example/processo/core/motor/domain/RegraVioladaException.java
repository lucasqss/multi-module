package com.example.processo.core.motor.domain;

/** Uma regra de negócio impediu a operação; o motivo é apresentável ao usuário. */
public class RegraVioladaException extends RuntimeException {

    public RegraVioladaException(String motivo) {
        super(motivo);
    }
}
