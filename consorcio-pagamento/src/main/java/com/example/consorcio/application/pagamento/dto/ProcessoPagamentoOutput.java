package com.example.consorcio.application.pagamento.dto;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Saída do caso de uso. A entidade de domínio nunca vaza para fora da application. */
public record ProcessoPagamentoOutput(
        String id,
        String cota,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDate dataVencimento,
        StatusProcessoPagamento status,
        Instant criadoEm) {

    public static ProcessoPagamentoOutput de(ProcessoPagamento processo) {
        return new ProcessoPagamentoOutput(
                processo.getId().toString(),
                processo.getCota().toString(),
                processo.getValor().valor(),
                processo.getFormaPagamento(),
                processo.getDataVencimento(),
                processo.getStatus(),
                processo.getCriadoEm());
    }
}
