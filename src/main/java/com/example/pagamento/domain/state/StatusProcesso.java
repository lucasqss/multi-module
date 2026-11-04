package com.example.pagamento.domain.state;

/**
 * Apenas o RÓTULO do estado do processo de pagamento (para persistir e expor na API).
 * O COMPORTAMENTO de cada estado está nas classes {@code Estado*} deste pacote.
 *
 * <pre>
 *  EM_EDICAO ─submeter─▶ AGUARDANDO_APROVACAO ─aprovar (alçada atingida)─▶ APROVADO
 *     ▲                     │  ↺ aprovar (falta aprovação)                     │ enviar ao banco
 *     └──reabrir── REJEITADO ◀──rejeitar                                       ▼
 *                                   FALHOU ◀── retorno: recusado ──── EM_PROCESSAMENTO ── retorno: liquidado ──▶ PAGO
 *                                     └──── retentar ───────────────────────▲      └── liquidado ≠ total ──▶ EM_ANALISE
 *
 *  Boleto cancelado no EMISSOR (fato): antes do envio → CANCELADO; depois do envio → EM_ANALISE.
 *  cancelar (usuário): qualquer estado não final → CANCELADO.
 * </pre>
 */
public enum StatusProcesso {
    EM_EDICAO,
    AGUARDANDO_APROVACAO,
    REJEITADO,
    APROVADO,
    EM_PROCESSAMENTO,
    FALHOU,
    PAGO,
    EM_ANALISE,
    CANCELADO
}
