package com.example.pagamento.entrypoint.api.v1.response;

import com.example.pagamento.application.pagamento.dto.PagamentoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o Output, e é de propósito: o Output pode mudar com a
 * application; ESTE só muda com versionamento da API. Enums viram String.
 */
public record PagamentoResponse(
        UUID id,
        String modalidade,
        String status,
        Instant criadoEm,
        ContaResponse contaDebito,
        LocalDate dataPagamento,
        BigDecimal valorTotal,
        List<BoletoResponse> boletos,
        TransacaoResponse transacao,
        AnaliseResponse analise,
        String motivoCancelamento) {

    public record ContaResponse(String agencia, String conta) {
    }

    public record BoletoResponse(String idNoEmissor, String linhaDigitavel, BigDecimal valor, LocalDate vencimento,
                                 String beneficiario) {
    }

    /** Último envio ao banco e o que ele respondeu. */
    public record TransacaoResponse(String id, int envios, String autenticacao, String motivoRecusa) {
    }

    /** Por que o pagamento está em análise (a regra não atendida pelo fato externo). */
    public record AnaliseResponse(String codigo, String mensagem) {
    }

    public static PagamentoResponse de(PagamentoOutput o) {
        return new PagamentoResponse(
                o.id(),
                o.modalidade().name(),
                o.status().name(),
                o.criadoEm(),
                o.agenciaDebito() == null ? null : new ContaResponse(o.agenciaDebito(), o.contaDebito()),
                o.dataPagamento(),
                o.valorTotal(),
                o.boletos().stream().map(b -> new BoletoResponse(b.idNoEmissor(), b.linhaDigitavel(), b.valor(),
                        b.vencimento(), b.beneficiario())).toList(),
                o.idTransacao() == null ? null
                        : new TransacaoResponse(o.idTransacao(), o.envios(), o.autenticacao(), o.motivoRecusa()),
                o.motivoAnaliseCodigo() == null ? null : new AnaliseResponse(o.motivoAnaliseCodigo(), o.motivoAnaliseMensagem()),
                o.motivoCancelamento());
    }
}
