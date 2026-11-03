package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.EmailInvalidoException;

/** Endereço de e-mail (validação de formato simplificada). Normalizado em minúsculas. */
public record Email(String valor) {

    public Email {
        if (valor == null || !valor.strip().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new EmailInvalidoException(valor);
        }
        valor = valor.strip().toLowerCase();
    }

    @Override
    public String toString() {
        return valor;
    }
}
