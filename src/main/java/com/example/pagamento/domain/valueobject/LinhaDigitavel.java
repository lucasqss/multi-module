package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.LinhaDigitavelInvalidaException;

import java.util.Objects;

/**
 * Linha digitável de um boleto (47 dígitos). Quem GERA é o sistema emissor (externo); nós só a
 * recebemos (do usuário ou do emissor) e validamos o formato.
 */
public record LinhaDigitavel(String valor) {

    public LinhaDigitavel {
        Objects.requireNonNull(valor);
        valor = valor.replaceAll("[\\s.]", "");
        if (!valor.matches("\\d{47}")) {
            throw new LinhaDigitavelInvalidaException();
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
