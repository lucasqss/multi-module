package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.state.StatusProcesso;

/** Critérios opcionais de busca do {@link ProcessoPagamentoRepository} (null = não filtra). */
public record FiltroProcessoPagamento(String solicitante, StatusProcesso status) {
}
