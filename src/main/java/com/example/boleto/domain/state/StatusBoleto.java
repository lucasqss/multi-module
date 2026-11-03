package com.example.boleto.domain.state;

/**
 * Apenas o RÓTULO do estado (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está nas classes {@code Estado*} deste pacote.
 *
 * <pre>
 *   EM_EDICAO ──emitir──▶ EMITIDO ──notificar──▶ NOTIFICADO ──receber pagamento──▶ PAGO
 *   (rascunho:              │  ╰───────── receber pagamento ─────────────────────────▲
 *    seções editáveis,      │                    │ ↺ notificar (reenvio)
 *    pode estar incompleto) └──── cancelar ──────┴──── cancelar ────▶ CANCELADO
 * </pre>
 */
public enum StatusBoleto {
    EM_EDICAO,
    EMITIDO,
    NOTIFICADO,
    PAGO,
    CANCELADO
}
