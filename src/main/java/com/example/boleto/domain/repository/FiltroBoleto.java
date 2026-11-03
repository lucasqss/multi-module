package com.example.boleto.domain.repository;

import com.example.boleto.domain.enums.StatusBoleto;

/** Critérios opcionais de busca (null = não filtra). */
public record FiltroBoleto(String documentoPagador, StatusBoleto status) {
}
