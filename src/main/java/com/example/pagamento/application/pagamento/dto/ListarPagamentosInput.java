package com.example.pagamento.application.pagamento.dto;

import com.example.pagamento.adapter.database.FiltroPagamento;
import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Modalidade;

/** Filtros opcionais (null = todos) e paginação. */
public record ListarPagamentosInput(Modalidade modalidade, StatusPagamento status, int pagina, int tamanho) {

    public FiltroPagamento filtro() {
        return new FiltroPagamento(modalidade, status);
    }
}
