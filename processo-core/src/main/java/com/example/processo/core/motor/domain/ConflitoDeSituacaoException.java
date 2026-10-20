package com.example.processo.core.motor.domain;

/** Outra requisição mudou a situação do processo entre a leitura e a gravação. */
public class ConflitoDeSituacaoException extends RuntimeException {

    public ConflitoDeSituacaoException(String numero) {
        super("Processo " + numero + " foi alterado por outra requisição; tente de novo");
    }
}
