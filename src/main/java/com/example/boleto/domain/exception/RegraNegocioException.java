package com.example.boleto.domain.exception;

/** Família: uma regra de negócio foi violada (lançada pelas specifications). */
public abstract class RegraNegocioException extends DomainException {

    protected RegraNegocioException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
