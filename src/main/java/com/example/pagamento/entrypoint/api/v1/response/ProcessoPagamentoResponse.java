package com.example.pagamento.entrypoint.api.v1.response;

import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o Output, e é de propósito: o Output pode mudar com a
 * application; ESTE só muda com versionamento da API. Enums viram String.
 */
public record ProcessoPagamentoResponse(
        UUID id,
        String status,
        String solicitante,
        Instant criadoEm,
        ContaResponse contaDebito,
        LocalDate dataPagamento,
        BigDecimal valorTotal,
        List<BoletoResponse> boletos,
        List<AprovacaoResponse> aprovacoes,
        List<TentativaResponse> tentativas,
        String motivoRejeicao,
        String motivoCancelamento,
        AnaliseResponse analise) {

    public record ContaResponse(String agencia, String conta) {
    }

    public record BoletoResponse(String idNoEmissor, String linhaDigitavel, BigDecimal valor, LocalDate vencimento,
                                 String beneficiario) {
    }

    public record AprovacaoResponse(String aprovador, Instant momento) {
    }

    public record TentativaResponse(int numero, String idTransacao, String status, Instant enviadaEm,
                                    BigDecimal valorLiquidado, String codigoAutenticacao, String motivoRecusa,
                                    Instant concluidaEm) {
    }

    /** Por que o processo está em análise (a regra não atendida por um fato externo). */
    public record AnaliseResponse(String codigo, String mensagem) {
    }

    public static ProcessoPagamentoResponse de(ProcessoPagamentoOutput o) {
        return new ProcessoPagamentoResponse(
                o.id(),
                o.status().name(),
                o.solicitante(),
                o.criadoEm(),
                o.agenciaDebito() == null ? null : new ContaResponse(o.agenciaDebito(), o.contaDebito()),
                o.dataPagamento(),
                o.valorTotal(),
                o.boletos().stream().map(b -> new BoletoResponse(b.idNoEmissor(), b.linhaDigitavel(), b.valor(),
                        b.vencimento(), b.beneficiario())).toList(),
                o.aprovacoes().stream().map(a -> new AprovacaoResponse(a.aprovador(), a.momento())).toList(),
                o.tentativas().stream().map(t -> new TentativaResponse(t.numero(), t.idTransacao(), t.status().name(),
                        t.enviadaEm(), t.valorLiquidado(), t.codigoAutenticacao(), t.motivoRecusa(),
                        t.concluidaEm())).toList(),
                o.motivoRejeicao(),
                o.motivoCancelamento(),
                o.motivoAnaliseCodigo() == null ? null : new AnaliseResponse(o.motivoAnaliseCodigo(), o.motivoAnaliseMensagem()));
    }
}
