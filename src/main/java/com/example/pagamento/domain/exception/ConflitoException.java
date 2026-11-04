package com.example.pagamento.domain.exception;

/** Família: a operação conflita com o estado atual do recurso (ação proibida no status, edição concorrente). */
public abstract class ConflitoException extends DomainException {

    protected ConflitoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
