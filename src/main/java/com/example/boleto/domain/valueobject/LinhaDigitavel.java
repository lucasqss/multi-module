package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.LinhaDigitavelInvalidaException;

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
            // também chega de fora (aviso do sistema de pagamentos): formato errado é dado inválido (400)
            throw new LinhaDigitavelInvalidaException();
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
