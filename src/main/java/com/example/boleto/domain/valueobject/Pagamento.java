package com.example.boleto.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

/** Pagamento informado pelo banco: quanto foi pago e quando. */
public record Pagamento(Dinheiro valorPago, LocalDate data) {

    public Pagamento {
        Objects.requireNonNull(valorPago, "valor pago é obrigatório");
        Objects.requireNonNull(data, "data do pagamento é obrigatória");
    }
}
