package com.example.pagamento.application.processo.dto;

import com.example.pagamento.domain.entity.ProcessoPagamento;
import com.example.pagamento.domain.state.StatusProcesso;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.Motivo;
import com.example.pagamento.domain.valueobject.MotivoAnalise;
import com.example.pagamento.domain.valueobject.StatusTentativa;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Saída dos casos de uso. A entidade de domínio nunca vaza para fora da application. */
public record ProcessoPagamentoOutput(
        UUID id,
        StatusProcesso status,
        String solicitante,
        Instant criadoEm,
        String agenciaDebito,
        String contaDebito,
        LocalDate dataPagamento,
        BigDecimal valorTotal,
        List<Boleto> boletos,
        List<Aprovacao> aprovacoes,
        List<Tentativa> tentativas,
        String motivoRejeicao,
        String motivoCancelamento,
        String motivoAnaliseCodigo,
        String motivoAnaliseMensagem) {

    public record Boleto(String idNoEmissor, String linhaDigitavel, BigDecimal valor, LocalDate vencimento,
                         String beneficiario) {
    }

    public record Aprovacao(String aprovador, Instant momento) {
    }

    public record Tentativa(int numero, String idTransacao, StatusTentativa status, Instant enviadaEm,
                            BigDecimal valorLiquidado, String codigoAutenticacao, String motivoRecusa,
                            Instant concluidaEm) {
    }

    public static ProcessoPagamentoOutput de(ProcessoPagamento p) {
        return new ProcessoPagamentoOutput(
                p.getId().valor(),
                p.getStatus(),
                p.getSolicitante().valor(),
                p.getCriadoEm(),
                p.getContaDebito().map(ContaDebito::agencia).orElse(null),
                p.getContaDebito().map(ContaDebito::conta).orElse(null),
                p.getDataPagamento().orElse(null),
                p.getValorTotal().valor(),
                p.getBoletos().boletos().stream().map(b -> new Boleto(b.idNoEmissor(), b.linhaDigitavel().valor(),
                        b.valor().valor(), b.vencimento(), b.beneficiario())).toList(),
                p.getAprovacoes().stream().map(a -> new Aprovacao(a.getAprovador().valor(), a.getMomento())).toList(),
                p.getTentativas().stream().map(t -> new Tentativa(t.getNumero(), t.getIdTransacao(), t.getStatus(),
                        t.getEnviadaEm(), t.getValorLiquidado().map(Dinheiro::valor).orElse(null),
                        t.getCodigoAutenticacao().orElse(null), t.getMotivoRecusa().orElse(null),
                        t.getConcluidaEm().orElse(null))).toList(),
                p.getMotivoRejeicao().map(Motivo::texto).orElse(null),
                p.getMotivoCancelamento().map(Motivo::texto).orElse(null),
                p.getMotivoAnalise().map(MotivoAnalise::codigo).orElse(null),
                p.getMotivoAnalise().map(MotivoAnalise::mensagem).orElse(null));
    }
}
