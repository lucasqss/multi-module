package com.example.consorcio.application.processopagamento.dto;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.valueobject.ContaDebito;
import com.example.consorcio.domain.valueobject.Motivo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Saída dos casos de uso. A entidade de domínio nunca vaza para fora da application. */
public record ProcessoPagamentoOutput(
        UUID id,
        int grupo,
        int cota,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito,
        StatusProcessoPagamento status,
        String motivoRejeicao,
        String motivoCancelamento,
        Instant criadoEm) {

    public static ProcessoPagamentoOutput de(ProcessoPagamento p) {
        return new ProcessoPagamentoOutput(
                p.getId().valor(),
                p.getCota().grupo(),
                p.getCota().cota(),
                p.getValor().valor(),
                p.getFormaPagamento(),
                p.getDataVencimento(),
                p.getContaDebito().map(ContaDebito::agencia).orElse(null),
                p.getContaDebito().map(ContaDebito::conta).orElse(null),
                p.getStatus(),
                p.getMotivoRejeicao().map(Motivo::texto).orElse(null),
                p.getMotivoCancelamento().map(Motivo::texto).orElse(null),
                p.getCriadoEm());
    }
}
