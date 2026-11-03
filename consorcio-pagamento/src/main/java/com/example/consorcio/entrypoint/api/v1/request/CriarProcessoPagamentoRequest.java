package com.example.consorcio.entrypoint.api.v1.request;

import com.example.consorcio.application.pagamento.dto.CriarProcessoPagamentoInput;
import com.example.consorcio.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato HTTP da v1. Aqui só há validação SINTÁTICA (campo veio? tipo certo?).
 * Validação de NEGÓCIO é do domínio: assim ela vale também para outros entrypoints.
 */
public record CriarProcessoPagamentoRequest(
        @NotNull Integer grupo,
        @NotNull Integer cota,
        @NotNull BigDecimal valor,
        @NotNull FormaPagamento formaPagamento,
        @NotNull LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {

    public CriarProcessoPagamentoInput paraInput() {
        return new CriarProcessoPagamentoInput(grupo, cota, valor, formaPagamento,
                dataVencimento, agenciaDebito, contaDebito);
    }
}
