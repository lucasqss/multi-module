package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.RegraNegocioException;
import jakarta.persistence.Embeddable;

/**
 * Por que o financiamento ficou pendente: o código e a mensagem da regra não atendida por um FATO
 * externo (a MESMA regra que, num comando, viraria erro 422).
 */
@Embeddable
public record MotivoPendencia(String codigo, String mensagem) {

    public static MotivoPendencia de(RegraNegocioException regraNaoAtendida) {
        return new MotivoPendencia(regraNaoAtendida.getCodigo(), regraNaoAtendida.getMessage());
    }
}
