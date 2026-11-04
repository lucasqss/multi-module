package com.example.pagamento.domain.valueobject;

import com.example.pagamento.domain.exception.RegraNegocioException;

/**
 * Por que o processo foi para análise: o código e a mensagem da regra não atendida por um FATO
 * externo (a MESMA regra que, num comando, viraria erro 422).
 */
public record MotivoAnalise(String codigo, String mensagem) {

    public static MotivoAnalise de(RegraNegocioException regraNaoAtendida) {
        return new MotivoAnalise(regraNaoAtendida.getCodigo(), regraNaoAtendida.getMessage());
    }
}
