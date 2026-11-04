package com.example.pagamento.domain.exception;

/** Família: o recurso procurado não existe. */
public abstract class RecursoNaoEncontradoException extends DomainException {

    protected RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
