package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.AlterarBoletoInput;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AlterarBoletoRequest(@NotNull LocalDate dataVencimento) {

    public AlterarBoletoInput paraInput(UUID id) {
        return new AlterarBoletoInput(id, dataVencimento);
    }
}
