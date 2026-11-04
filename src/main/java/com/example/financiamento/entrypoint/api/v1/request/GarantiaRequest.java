package com.example.financiamento.entrypoint.api.v1.request;

import jakarta.validation.constraints.NotBlank;

/** Seção "garantia": só a identificação do bem (matrícula ou placa); os dados vêm do sistema de avaliação. */
public record GarantiaRequest(@NotBlank String identificacao) {
}
