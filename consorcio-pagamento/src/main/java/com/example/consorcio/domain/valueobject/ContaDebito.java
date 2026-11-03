package com.example.consorcio.domain.valueobject;

import com.example.consorcio.domain.exception.RegraNegocioException;

/** Conta corrente usada para débito automático. */
public record ContaDebito(String agencia, String conta) {

    public ContaDebito {
        if (agencia == null || !agencia.matches("\\d{4}")) {
            throw new RegraNegocioException("AGENCIA_INVALIDA", "Agência deve ter 4 dígitos");
        }
        if (conta == null || !conta.matches("\\d{1,12}")) {
            throw new RegraNegocioException("CONTA_INVALIDA", "Conta deve ter de 1 a 12 dígitos");
        }
    }
}
