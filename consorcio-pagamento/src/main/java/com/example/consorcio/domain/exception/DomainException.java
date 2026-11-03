package com.example.consorcio.domain.exception;

/**
 * Base das exceções do domínio. Não conhece HTTP: quem traduz para status code
 * é o entrypoint (ver {@code DomainExceptionMapper}).
 */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
