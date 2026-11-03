package com.example.consorcio.domain.repository;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Critérios opcionais de busca (null = não filtra). */
public record FiltroProcessoPagamento(Integer grupo, Integer cota, StatusProcessoPagamento status) {
}
