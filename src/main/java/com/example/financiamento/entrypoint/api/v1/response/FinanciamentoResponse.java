package com.example.financiamento.entrypoint.api.v1.response;

import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contrato HTTP da v1. Parece repetir o Output, e é de propósito: o Output pode mudar com a
 * application; ESTE só muda com versionamento da API. Enums viram String.
 */
public record FinanciamentoResponse(
        UUID id,
        String tipo,
        String status,
        Instant criadoEm,
        ProponenteResponse proponente,
        BigDecimal valorSolicitado,
        Integer prazoMeses,
        BemResponse garantia,
        List<DocumentoResponse> documentos,
        RegistroResponse registro,
        PendenciaResponse pendencia,
        String motivoCancelamento) {

    public record ProponenteResponse(String nome, String cpf, BigDecimal rendaMensal) {
    }

    public record BemResponse(String identificacao, String descricao, BigDecimal valorAvaliado, Integer anoModelo) {
    }

    public record DocumentoResponse(String tipo, String referencia) {
    }

    /** Último envio da garantia ao órgão e o que ele respondeu. */
    public record RegistroResponse(String protocolo, int envios, String numeroRegistro, String motivoRecusa) {
    }

    /** Por que o financiamento está pendente (a regra não atendida pelo fato externo). */
    public record PendenciaResponse(String codigo, String mensagem) {
    }

    public static FinanciamentoResponse de(FinanciamentoOutput o) {
        return new FinanciamentoResponse(
                o.id(),
                o.tipo().name(),
                o.status().name(),
                o.criadoEm(),
                o.nomeProponente() == null ? null : new ProponenteResponse(o.nomeProponente(), o.cpfProponente(), o.rendaMensal()),
                o.valorSolicitado(),
                o.prazoMeses(),
                o.bem() == null ? null : new BemResponse(o.bem().identificacao(), o.bem().descricao(),
                        o.bem().valorAvaliado(), o.bem().anoModelo()),
                o.documentos().stream().map(d -> new DocumentoResponse(d.tipo().name(), d.referencia())).toList(),
                o.idProtocolo() == null ? null
                        : new RegistroResponse(o.idProtocolo(), o.envios(), o.numeroRegistro(), o.motivoRecusa()),
                o.motivoPendenciaCodigo() == null ? null : new PendenciaResponse(o.motivoPendenciaCodigo(), o.motivoPendenciaMensagem()),
                o.motivoCancelamento());
    }
}
