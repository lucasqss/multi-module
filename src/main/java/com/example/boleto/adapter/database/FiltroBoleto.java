package com.example.boleto.adapter.database;

import com.example.boleto.domain.enums.StatusBoleto;

/** Critérios opcionais de busca do {@link BoletoRepository} (null = não filtra). */
public record FiltroBoleto(String documentoPagador, StatusBoleto status) {
}
