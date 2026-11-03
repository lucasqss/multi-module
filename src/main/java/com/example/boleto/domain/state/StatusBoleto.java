package com.example.boleto.domain.state;

/**
 * Apenas o RÓTULO do estado (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está em {@code domain.state}.
 *
 * <pre>
 *            notificar             receber pagamento
 *   EMITIDO ───────────▶ NOTIFICADO ─────────────────▶ PAGO
 *      │  ╰──────────── receber pagamento ─────────────▲
 *      │                     │  ↺ notificar (reenvio)
 *      └──── cancelar ───────┴──── cancelar ────▶ CANCELADO
 * </pre>
 */
public enum StatusBoleto {
    EMITIDO,
    NOTIFICADO,
    PAGO,
    CANCELADO
}
