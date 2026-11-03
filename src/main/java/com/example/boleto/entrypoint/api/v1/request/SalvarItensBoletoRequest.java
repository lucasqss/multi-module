package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.SalvarItensBoletoInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** A seção inteira; lista vazia é permitida (limpa a seção). */
public record SalvarItensBoletoRequest(@NotNull List<@Valid @NotNull ItemRequest> itens) {

    public record ItemRequest(@NotBlank String descricao, @NotNull BigDecimal valor) {
    }

    public SalvarItensBoletoInput paraInput(UUID id) {
        return new SalvarItensBoletoInput(id,
                itens.stream().map(i -> new SalvarItensBoletoInput.Item(i.descricao(), i.valor())).toList());
    }
}
