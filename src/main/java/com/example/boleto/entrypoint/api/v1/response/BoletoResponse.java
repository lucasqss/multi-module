package com.example.boleto.entrypoint.api.v1.response;

import com.example.boleto.application.boleto.dto.BoletoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o {@link BoletoOutput}, e é de propósito:
 * o Output pode mudar com as necessidades da application; ESTE só muda com versionamento da API.
 * Enums viram String aqui para o contrato não depender de tipos do domínio.
 */
public record BoletoResponse(
        UUID id,
        PagadorResponse pagador,
        BigDecimal valor,
        LocalDate dataVencimento,
        String linhaDigitavel,
        String status,
        Instant emitidoEm,
        Instant notificadoEm,
        PagamentoResponse pagamento,
        String motivoCancelamento) {

    public record PagadorResponse(String nome, String documento, String email) {
    }

    public record PagamentoResponse(BigDecimal valorPago, LocalDate dataPagamento) {
    }

    public static BoletoResponse de(BoletoOutput o) {
        var pagamento = o.valorPago() == null ? null : new PagamentoResponse(o.valorPago(), o.dataPagamento());
        return new BoletoResponse(
                o.id(),
                new PagadorResponse(o.nomePagador(), o.documentoPagador(), o.emailPagador()),
                o.valor(),
                o.dataVencimento(),
                o.linhaDigitavel(),
                o.status().name(),
                o.emitidoEm(),
                o.notificadoEm(),
                pagamento,
                o.motivoCancelamento());
    }
}
