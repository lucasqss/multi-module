package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.MatriculaInvalidaException;

/** Identifica uma pessoa da empresa (solicitante, aprovador). Formato fake: letra + 7 dígitos. */
public record Matricula(String valor) {

    public Matricula {
        if (valor == null || !valor.strip().toUpperCase().matches("[A-Z]\\d{7}")) {
            throw new MatriculaInvalidaException(valor);
        }
        valor = valor.strip().toUpperCase();
    }

    @Override
    public String toString() {
        return valor;
    }
}
