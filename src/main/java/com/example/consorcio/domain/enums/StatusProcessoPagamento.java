package com.example.consorcio.domain.enums;

/**
 * Apenas o RÓTULO do estado (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está em {@code domain.state}.
 *
 * <pre>
 *   ABERTO ──enviar──▶ EM_PROCESSAMENTO ──confirmar──▶ CONFIRMADO
 *     │  ▲                     │
 *     │  └──────reabrir─── REJEITADO ◀──rejeitar──┘
 *     │                        │
 *     └──cancelar──▶ CANCELADO ◀──cancelar──┘
 * </pre>
 */
public enum StatusProcessoPagamento {
    ABERTO,
    EM_PROCESSAMENTO,
    REJEITADO,
    CONFIRMADO,
    CANCELADO
}
