package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Modalidade;

/** Critérios opcionais de busca do {@link PagamentoRepository} (null = não filtra). */
public record FiltroPagamento(Modalidade modalidade, StatusPagamento status) {
}
