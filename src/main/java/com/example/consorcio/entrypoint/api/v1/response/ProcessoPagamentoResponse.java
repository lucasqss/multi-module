package com.example.consorcio.entrypoint.api.v1.response;

import com.example.consorcio.application.processopagamento.dto.ProcessoPagamentoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o {@link ProcessoPagamentoOutput}, e é de propósito:
 * o Output pode mudar com as necessidades da application; ESTE só muda com versionamento da API.
 * Enums viram String aqui para o contrato não depender de tipos do domínio.
 */
public record ProcessoPagamentoResponse(
        UUID id,
        int grupo,
        int cota,
        BigDecimal valor,
        String formaPagamento,
        LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito,
        String status,
        String motivoRejeicao,
        String motivoCancelamento,
        Instant criadoEm) {

    public static ProcessoPagamentoResponse de(ProcessoPagamentoOutput o) {
        return new ProcessoPagamentoResponse(o.id(), o.grupo(), o.cota(), o.valor(), o.formaPagamento().name(),
                o.dataVencimento(), o.agenciaDebito(), o.contaDebito(), o.status().name(),
                o.motivoRejeicao(), o.motivoCancelamento(), o.criadoEm());
    }
}
