package com.example.consorcio.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ProcessoPagamentoId(UUID valor) {

    public ProcessoPagamentoId {
        Objects.requireNonNull(valor, "id é obrigatório");
    }

    public static ProcessoPagamentoId novo() {
        return new ProcessoPagamentoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
