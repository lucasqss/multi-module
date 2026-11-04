package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.ContaDebitoInvalidaException;

/** Conta da empresa de onde sai o dinheiro. */
public record ContaDebito(String agencia, String conta) {

    public ContaDebito {
        if (agencia == null || !agencia.matches("\\d{4}")) {
            throw new ContaDebitoInvalidaException("agência deve ter 4 dígitos");
        }
        if (conta == null || !conta.matches("\\d{1,12}")) {
            throw new ContaDebitoInvalidaException("conta deve ter de 1 a 12 dígitos");
        }
    }
}
