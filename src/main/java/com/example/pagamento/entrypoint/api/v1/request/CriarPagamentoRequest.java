package com.example.pagamento.entrypoint.api.v1.request;

import com.example.pagamento.domain.valueobject.Modalidade;
import jakarta.validation.constraints.NotNull;

/** Contrato HTTP. Só validação SINTÁTICA (400); regra de negócio é do domínio. */
public record CriarPagamentoRequest(@NotNull Modalidade modalidade) {
}
