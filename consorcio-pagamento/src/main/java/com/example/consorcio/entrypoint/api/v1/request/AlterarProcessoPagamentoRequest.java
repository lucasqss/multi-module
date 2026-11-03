package com.example.consorcio.entrypoint.api.v1.request;

import com.example.consorcio.application.processopagamento.dto.AlterarProcessoPagamentoInput;
import com.example.consorcio.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AlterarProcessoPagamentoRequest(
        @NotNull BigDecimal valor,
        @NotNull FormaPagamento formaPagamento,
        @NotNull LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {

    public AlterarProcessoPagamentoInput paraInput(UUID id) {
        return new AlterarProcessoPagamentoInput(id, valor, formaPagamento, dataVencimento, agenciaDebito, contaDebito);
    }
}
