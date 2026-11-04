package com.example.pagamento.domain.state;

/**
 * Apenas o RÓTULO do estado (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está nas classes {@code Estado*} deste pacote.
 *
 * <pre>
 *  EM_EDICAO ─submeter─▶ AGENDADO ─enviar─▶ EM_PROCESSAMENTO ── retorno: liquidado conforme ──▶ PAGO
 *                                                 ├── retorno: recusado ──▶ RECUSADO ─reenviar─┐
 *                                                 │       ▲────────────────────────────────────┘
 *                                                 └── retorno: liquidado ≠ total ──▶ EM_ANALISE
 *  cancelar: EM_EDICAO, AGENDADO, RECUSADO e EM_ANALISE ──▶ CANCELADO
 * </pre>
 */
public enum StatusPagamento {
    EM_EDICAO,
    AGENDADO,
    EM_PROCESSAMENTO,
    RECUSADO,
    PAGO,
    EM_ANALISE,
    CANCELADO
}
