package com.example.financiamento.entrypoint.api.v1.request;

import com.example.financiamento.domain.valueobject.TipoFinanciamento;
import jakarta.validation.constraints.NotNull;

/** Contrato HTTP. Só validação SINTÁTICA (400); regra de negócio é do domínio. */
public record CriarFinanciamentoRequest(@NotNull TipoFinanciamento tipo) {
}
