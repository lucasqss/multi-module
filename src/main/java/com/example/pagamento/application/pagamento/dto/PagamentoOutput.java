package com.example.pagamento.application.pagamento.dto;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.IdTransacao;
import com.example.pagamento.domain.valueobject.Modalidade;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Saída dos casos de uso: dados planos, sem expor a entidade para fora da application. */
public record PagamentoOutput(
        UUID id,
        Modalidade modalidade,
        StatusPagamento status,
        Instant criadoEm,
        String agenciaDebito,
        String contaDebito,
        LocalDate dataPagamento,
        BigDecimal valorTotal,
        List<BoletoOutput> boletos,
        int envios,
        String idTransacao,
        String autenticacao,
        String motivoRecusa,
        String motivoAnaliseCodigo,
        String motivoAnaliseMensagem,
        String motivoCancelamento) {

    public record BoletoOutput(String idNoEmissor, String linhaDigitavel, BigDecimal valor, LocalDate vencimento,
                               String beneficiario) {
    }

    public static PagamentoOutput de(Pagamento p) {
        return new PagamentoOutput(
                p.getId().valor(),
                p.getModalidade(),
                p.getStatus(),
                p.getCriadoEm(),
                p.getContaDebito().map(ContaDebito::agencia).orElse(null),
                p.getContaDebito().map(ContaDebito::conta).orElse(null),
                p.getDataPagamento().orElse(null),
                p.getValorTotal().valor(),
                p.getBoletos().boletos().stream().map(b -> new BoletoOutput(b.idNoEmissor(), b.linhaDigitavel().valor(),
                        b.valor().valor(), b.vencimento(), b.beneficiario())).toList(),
                p.getEnvios(),
                p.getIdTransacao().map(IdTransacao::toString).orElse(null),
                p.getAutenticacao().orElse(null),
                p.getMotivoRecusa().orElse(null),
                p.getMotivoAnalise().map(MotivoAnalise::codigo).orElse(null),
                p.getMotivoAnalise().map(MotivoAnalise::mensagem).orElse(null),
                p.getMotivoCancelamento().map(Motivo::texto).orElse(null));
    }
}
