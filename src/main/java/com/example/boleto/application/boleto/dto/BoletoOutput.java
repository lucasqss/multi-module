package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.entity.Boleto;
import com.example.boleto.domain.enums.StatusBoleto;
import com.example.boleto.domain.valueobject.Motivo;
import com.example.boleto.domain.valueobject.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Saída dos casos de uso. A entidade de domínio nunca vaza para fora da application. */
public record BoletoOutput(
        UUID id,
        String nomePagador,
        String documentoPagador,
        String emailPagador,
        BigDecimal valor,
        LocalDate dataVencimento,
        String linhaDigitavel,
        StatusBoleto status,
        Instant emitidoEm,
        Instant notificadoEm,
        BigDecimal valorPago,
        LocalDate dataPagamento,
        String motivoCancelamento) {

    public static BoletoOutput de(Boleto b) {
        return new BoletoOutput(
                b.getId().valor(),
                b.getPagador().nome(),
                b.getPagador().documento().numero(),
                b.getPagador().email(),
                b.getValor().valor(),
                b.getDataVencimento(),
                b.getLinhaDigitavel().valor(),
                b.getStatus(),
                b.getEmitidoEm(),
                b.getNotificadoEm().orElse(null),
                b.getPagamento().map(p -> p.valorPago().valor()).orElse(null),
                b.getPagamento().map(Pagamento::data).orElse(null),
                b.getMotivoCancelamento().map(Motivo::texto).orElse(null));
    }
}
