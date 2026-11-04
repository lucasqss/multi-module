package com.example.pagamento.application.processo.dto;

import com.example.pagamento.adapter.database.FiltroProcessoPagamento;
import com.example.pagamento.domain.state.StatusProcesso;

public record ListarProcessosInput(String solicitante, StatusProcesso status, int pagina, int tamanho) {

    public FiltroProcessoPagamento filtro() {
        return new FiltroProcessoPagamento(solicitante, status);
    }
}
