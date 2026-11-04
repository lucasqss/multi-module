package com.example.financiamento.application.financiamento.dto;

import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.IdProtocolo;
import com.example.financiamento.domain.valueobject.Motivo;
import com.example.financiamento.domain.valueobject.MotivoPendencia;
import com.example.financiamento.domain.valueobject.TipoDocumento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Saída dos casos de uso: dados planos, sem expor a entidade para fora da application. */
public record FinanciamentoOutput(
        UUID id,
        TipoFinanciamento tipo,
        StatusFinanciamento status,
        Instant criadoEm,
        String nomeProponente,
        String cpfProponente,
        BigDecimal rendaMensal,
        BigDecimal valorSolicitado,
        Integer prazoMeses,
        BemOutput bem,
        List<DocumentoOutput> documentos,
        int envios,
        String idProtocolo,
        String numeroRegistro,
        String motivoRecusa,
        String motivoPendenciaCodigo,
        String motivoPendenciaMensagem,
        String motivoCancelamento) {

    public record BemOutput(String identificacao, String descricao, BigDecimal valorAvaliado, Integer anoModelo) {
    }

    public record DocumentoOutput(TipoDocumento tipo, String referencia) {
    }

    public static FinanciamentoOutput de(Financiamento f) {
        var proponente = f.getProponente();
        return new FinanciamentoOutput(
                f.getId(),
                f.getTipo(),
                f.getStatus(),
                f.getCriadoEm(),
                proponente.map(p -> p.nome()).orElse(null),
                proponente.map(p -> p.cpf().valor()).orElse(null),
                proponente.map(p -> p.rendaMensal().valor()).orElse(null),
                f.getValorSolicitado().map(Dinheiro::valor).orElse(null),
                f.getPrazoMeses().orElse(null),
                f.getBem().map(FinanciamentoOutput::bem).orElse(null),
                f.getDocumentos().documentos().stream().map(d -> new DocumentoOutput(d.tipo(), d.referencia())).toList(),
                f.getEnvios(),
                f.getIdProtocolo().map(IdProtocolo::toString).orElse(null),
                f.getNumeroRegistro().orElse(null),
                f.getMotivoRecusa().orElse(null),
                f.getMotivoPendencia().map(MotivoPendencia::codigo).orElse(null),
                f.getMotivoPendencia().map(MotivoPendencia::mensagem).orElse(null),
                f.getMotivoCancelamento().map(Motivo::texto).orElse(null));
    }

    private static BemOutput bem(Bem b) {
        return new BemOutput(b.identificacao(), b.descricao(), b.valorAvaliado().valor(), b.anoModelo());
    }
}
