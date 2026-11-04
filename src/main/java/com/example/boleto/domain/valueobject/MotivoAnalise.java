package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.RegraNegocioException;

/**
 * Por que um pagamento recebido foi para análise: o código e a mensagem da regra de conformidade
 * que ele não atendeu (a MESMA regra que, num comando, viraria erro 422).
 */
public record MotivoAnalise(String codigo, String mensagem) {

    public static MotivoAnalise de(RegraNegocioException regraNaoAtendida) {
        return new MotivoAnalise(regraNaoAtendida.getCodigo(), regraNaoAtendida.getMessage());
    }
}
