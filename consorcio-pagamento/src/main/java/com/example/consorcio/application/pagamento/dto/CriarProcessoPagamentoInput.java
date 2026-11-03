package com.example.consorcio.application.pagamento.dto;

import com.example.consorcio.domain.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados de entrada do caso de uso, já independentes do protocolo (REST, fila, IIB...). */
public record CriarProcessoPagamentoInput(
        int grupo,
        int cota,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {
}
