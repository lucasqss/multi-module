package com.example.consorcio.application.processopagamento.dto;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.repository.FiltroProcessoPagamento;

public record ListarProcessosPagamentoInput(
        Integer grupo,
        Integer cota,
        StatusProcessoPagamento status,
        int pagina,
        int tamanho) {

    public FiltroProcessoPagamento filtro() {
        return new FiltroProcessoPagamento(grupo, cota, status);
    }
}
