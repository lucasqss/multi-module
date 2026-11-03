package com.example.boleto.domain.valueobject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Linha digitável do boleto (47 dígitos).
 *
 * <p>Geração FAKE, só para a simulação (não segue o padrão FEBRABAN):
 * {@code 9999} (banco/moeda fictícios) + 25 dígitos derivados do id + vencimento {@code yyyyMMdd}
 * + valor em centavos (10 dígitos). Muda sempre que valor ou vencimento mudam.
 */
public record LinhaDigitavel(String valor) {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyyMMdd");

    public LinhaDigitavel {
        Objects.requireNonNull(valor);
        if (!valor.matches("\\d{47}")) {
            // só nós geramos linha digitável: formato errado é bug, não erro do usuário
            throw new IllegalArgumentException("Linha digitável deve ter 47 dígitos");
        }
    }

    public static LinhaDigitavel gerar(BoletoId id, Dinheiro valor, LocalDate vencimento) {
        var identificador = "%025d".formatted(Math.abs(id.valor().getMostSignificantBits() % 1_000_000_000_000_000L));
        var centavos = "%010d".formatted(valor.valor().movePointRight(2).longValueExact());
        return new LinhaDigitavel("9999" + identificador + DATA.format(vencimento) + centavos);
    }

    @Override
    public String toString() {
        return valor;
    }
}
