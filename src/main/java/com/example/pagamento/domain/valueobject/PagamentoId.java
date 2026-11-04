package com.example.pagamento.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record PagamentoId(UUID valor) {

    public PagamentoId {
        Objects.requireNonNull(valor, "id é obrigatório");
    }

    public static PagamentoId novo() {
        return new PagamentoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
