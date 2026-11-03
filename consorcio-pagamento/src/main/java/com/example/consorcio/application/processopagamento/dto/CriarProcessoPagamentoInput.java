package com.example.consorcio.application.processopagamento.dto;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.IdentificadorCota;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entrada do caso de uso, independente do protocolo (REST, fila, IIB...). */
public record CriarProcessoPagamentoInput(
        int grupo,
        int cota,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {

    public IdentificadorCota identificadorCota() {
        return new IdentificadorCota(grupo, cota);
    }

    public DadosPagamento dadosPagamento() {
        return DadosPagamento.de(valor, formaPagamento, dataVencimento, agenciaDebito, contaDebito);
    }
}
