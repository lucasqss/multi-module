package com.example.financiamento.entrypoint.api.v1.request;

import jakarta.validation.constraints.NotBlank;

public record MotivoRequest(@NotBlank String motivo) {
}
