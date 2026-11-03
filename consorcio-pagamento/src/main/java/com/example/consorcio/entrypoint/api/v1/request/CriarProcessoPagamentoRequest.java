package com.example.consorcio.entrypoint.api.v1.request;

import com.example.consorcio.application.processopagamento.dto.CriarProcessoPagamentoInput;
import com.example.consorcio.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato HTTP. Aqui só há validação SINTÁTICA (o campo veio?) -> 400.
 * Validação de NEGÓCIO é das specifications -> 422, e vale para qualquer entrypoint.
 */
public record CriarProcessoPagamentoRequest(
        @NotNull Integer grupo,
        @NotNull Integer cota,
        @NotNull BigDecimal valor,
        @NotNull FormaPagamento formaPagamento,
        @NotNull LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {

    public CriarProcessoPagamentoInput paraInput() {
        return new CriarProcessoPagamentoInput(grupo, cota, valor, formaPagamento, dataVencimento,
                agenciaDebito, contaDebito);
    }
}
