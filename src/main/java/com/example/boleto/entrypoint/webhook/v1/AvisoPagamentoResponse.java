package com.example.boleto.entrypoint.webhook.v1;

import com.example.boleto.application.boleto.dto.BoletoOutput;

import java.util.UUID;

/** Resposta mínima para o sistema externo: ele só precisa saber que o aviso foi processado. */
public record AvisoPagamentoResponse(UUID boletoId, String status) {

    public static AvisoPagamentoResponse de(BoletoOutput o) {
        return new AvisoPagamentoResponse(o.id(), o.status().name());
    }
}
