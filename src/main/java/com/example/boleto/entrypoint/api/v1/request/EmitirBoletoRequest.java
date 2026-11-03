package com.example.boleto.entrypoint.api.v1.request;

import com.example.boleto.application.boleto.dto.EmitirBoletoInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato HTTP. Aqui só há validação SINTÁTICA (o campo veio?) -> 400.
 * Validação de NEGÓCIO é das specifications -> 422, e vale para qualquer entrypoint.
 */
public record EmitirBoletoRequest(
        @Valid @NotNull PagadorRequest pagador,
        @NotNull BigDecimal valor,
        @NotNull LocalDate dataVencimento) {

    public record PagadorRequest(@NotBlank String nome, @NotBlank String documento, @NotBlank String email) {
    }

    public EmitirBoletoInput paraInput() {
        return new EmitirBoletoInput(pagador.nome(), pagador.documento(), pagador.email(), valor, dataVencimento);
    }
}
