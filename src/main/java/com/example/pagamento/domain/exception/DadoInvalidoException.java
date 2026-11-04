package com.example.pagamento.domain.exception;

/** Família: o dado não tem formato válido (lançada pelos value objects). */
public abstract class DadoInvalidoException extends DomainException {

    protected DadoInvalidoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
