package com.example.pagamento.entrypoint.api.v1.request;

import com.example.pagamento.application.processo.dto.SalvarBoletosInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** A seção inteira: só as linhas digitáveis (os dados vêm do emissor). Lista vazia limpa a seção. */
public record SalvarBoletosRequest(@NotNull List<@NotBlank String> linhasDigitaveis) {

    public SalvarBoletosInput paraInput(UUID id) {
        return new SalvarBoletosInput(id, linhasDigitaveis);
    }
}
