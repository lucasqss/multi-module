package com.example.pagamento.entrypoint.api.v1.request;

import jakarta.validation.constraints.NotBlank;

/** Contrato HTTP. Só validação SINTÁTICA (400); formato/negócio é do domínio. */
public record CriarProcessoRequest(@NotBlank String solicitante) {
}
