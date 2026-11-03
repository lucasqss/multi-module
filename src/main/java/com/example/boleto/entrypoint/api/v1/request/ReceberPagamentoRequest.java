package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.ReceberPagamentoInput;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Simula o aviso de pagamento que o banco enviaria. */
public record ReceberPagamentoRequest(@NotNull BigDecimal valorPago, @NotNull LocalDate dataPagamento) {

    public ReceberPagamentoInput paraInput(UUID id) {
        return new ReceberPagamentoInput(id, valorPago, dataPagamento);
    }
}
