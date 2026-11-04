package com.example.boleto.entrypoint.webhook.v1;

import com.example.boleto.application.boleto.dto.ReceberPagamentoInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Contrato definido pelo SISTEMA EXTERNO de pagamentos (não por nós): valor em centavos, data/hora com
 * fuso. A tradução para o formato da application fica aqui, na borda; o domínio nunca vê centavos.
 */
public record AvisoPagamentoRequest(
        @NotBlank String idTransacao,
        @NotBlank String linhaDigitavel,
        @NotNull @Positive Long valorPagoCentavos,
        @NotNull OffsetDateTime dataHoraPagamento) {

    public ReceberPagamentoInput paraInput() {
        return new ReceberPagamentoInput(linhaDigitavel, idTransacao,
                BigDecimal.valueOf(valorPagoCentavos).movePointLeft(2), dataHoraPagamento.toInstant());
    }
}
