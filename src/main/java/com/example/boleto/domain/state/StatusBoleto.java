package com.example.boleto.domain.state;

/**
 * Apenas o RÓTULO do estado (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está nas classes {@code Estado*} deste pacote.
 *
 * <pre>
 *  EM_EDICAO ─emitir─▶ EMITIDO ─notificar─▶ NOTIFICADO (↺ reenvio)
 *                         │                     │
 *                         │  pagamento recebido (webhook do sistema externo)
 *                         ├── conforme ───────▶ PAGO
 *                         └── divergente ─────▶ EM_ANALISE ─aceitar─▶ PAGO
 *                         │                         └──────devolver──▶ DEVOLVIDO
 *                         └─cancelar─▶ CANCELADO ──pagamento recebido──▶ EM_ANALISE
 * </pre>
 */
public enum StatusBoleto {
    EM_EDICAO,
    EMITIDO,
    NOTIFICADO,
    PAGO,
    EM_ANALISE,
    DEVOLVIDO,
    CANCELADO
}
