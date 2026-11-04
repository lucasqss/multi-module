package com.example.pagamento.entrypoint.api.v1.request;

import jakarta.validation.constraints.NotBlank;

/** Na vida real o aprovador viria do token de autenticação; aqui vem no corpo para simplificar. */
public record AprovacaoRequest(@NotBlank String aprovador) {
}
