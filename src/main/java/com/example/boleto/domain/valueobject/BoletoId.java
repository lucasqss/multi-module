package com.example.boleto.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record BoletoId(UUID valor) {

    public BoletoId {
        Objects.requireNonNull(valor, "id é obrigatório");
    }

    public static BoletoId novo() {
        return new BoletoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
