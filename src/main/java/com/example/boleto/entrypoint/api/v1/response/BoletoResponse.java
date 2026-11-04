package com.example.boleto.entrypoint.api.v1.response;

import com.example.boleto.application.boleto.dto.BoletoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o {@link BoletoOutput}, e é de propósito:
 * o Output pode mudar com as necessidades da application; ESTE só muda com versionamento da API.
 * Enums viram String aqui para o contrato não depender de tipos do domínio.
 */
public record BoletoResponse(
        UUID id,
        String status,
        PagadorResponse pagador,
        LocalDate dataVencimento,
        List<ItemResponse> itens,
        BigDecimal valor,
        List<String> destinatarios,
        String linhaDigitavel,
        Instant criadoEm,
        Instant emitidoEm,
        Instant notificadoEm,
        PagamentoResponse pagamento,
        AnaliseResponse analise,
        String motivoDevolucao,
        String motivoCancelamento) {

    public record PagadorResponse(String nome, String documento, String email) {
    }

    public record ItemResponse(String descricao, BigDecimal valor) {
    }

    public record PagamentoResponse(String idTransacao, BigDecimal valorPago, LocalDate dataPagamento) {
    }

    /** Por que o pagamento foi para análise (a regra de conformidade não atendida). */
    public record AnaliseResponse(String codigo, String mensagem) {
    }

    public static BoletoResponse de(BoletoOutput o) {
        var pagamento = o.valorPago() == null ? null
                : new PagamentoResponse(o.idTransacaoPagamento(), o.valorPago(), o.dataPagamento());
        var analise = o.motivoAnaliseCodigo() == null ? null
                : new AnaliseResponse(o.motivoAnaliseCodigo(), o.motivoAnaliseMensagem());
        return new BoletoResponse(
                o.id(),
                o.status().name(),
                new PagadorResponse(o.nomePagador(), o.documentoPagador(), o.emailPagador()),
                o.dataVencimento(),
                o.itens().stream().map(i -> new ItemResponse(i.descricao(), i.valor())).toList(),
                o.valor(),
                o.destinatarios(),
                o.linhaDigitavel(),
                o.criadoEm(),
                o.emitidoEm(),
                o.notificadoEm(),
                pagamento,
                analise,
                o.motivoDevolucao(),
                o.motivoCancelamento());
    }
}
