package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.SalvarDestinatariosBoletoInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** A seção inteira; lista vazia é permitida (remove os destinatários extras). */
public record SalvarDestinatariosBoletoRequest(@NotNull List<@NotBlank String> emails) {

    public SalvarDestinatariosBoletoInput paraInput(UUID id) {
        return new SalvarDestinatariosBoletoInput(id, emails);
    }
}
