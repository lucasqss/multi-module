package com.example.consorcio.domain.exception;

/** Família: o recurso procurado não existe. */
public abstract class RecursoNaoEncontradoException extends DomainException {

    protected RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
