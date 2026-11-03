package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.CriarBoletoInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Contrato HTTP. Aqui só há validação SINTÁTICA (o campo veio?) -> 400.
 * Validação de NEGÓCIO é das specifications -> 422, e vale para qualquer entrypoint.
 */
public record CriarBoletoRequest(@Valid @NotNull PagadorRequest pagador, @NotNull LocalDate dataVencimento) {

    public record PagadorRequest(@NotBlank String nome, @NotBlank String documento, @NotBlank String email) {
    }

    public CriarBoletoInput paraInput() {
        return new CriarBoletoInput(pagador.nome(), pagador.documento(), pagador.email(), dataVencimento);
    }
}
