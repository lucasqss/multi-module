package com.example.consorcio.domain.exception;

/** Uma regra/invariante de negócio foi violada. */
public class RegraNegocioException extends DomainException {

    public RegraNegocioException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
