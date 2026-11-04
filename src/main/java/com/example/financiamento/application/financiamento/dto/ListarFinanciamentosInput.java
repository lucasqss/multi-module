package com.example.financiamento.application.financiamento.dto;

import com.example.financiamento.domain.state.StatusFinanciamento;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;

/** Filtros opcionais (null = todos) e paginação. */
public record ListarFinanciamentosInput(TipoFinanciamento tipo, StatusFinanciamento status, int pagina, int tamanho) {
}
