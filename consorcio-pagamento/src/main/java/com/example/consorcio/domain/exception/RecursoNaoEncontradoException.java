package com.example.consorcio.domain.exception;

public class RecursoNaoEncontradoException extends DomainException {

    public RecursoNaoEncontradoException(String recurso, Object identificador) {
        super("NAO_ENCONTRADO", recurso + " não encontrado(a): " + identificador);
    }
}
