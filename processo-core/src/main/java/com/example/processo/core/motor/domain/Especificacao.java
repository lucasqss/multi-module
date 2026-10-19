package com.example.processo.core.motor.domain;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Regra de negócio sobre um candidato. Devolve o motivo quando a regra é violada, para que uma composição de
 * regras diga exatamente qual falhou.
 */
@FunctionalInterface
public interface Especificacao<T> {

    Optional<String> violacao(T candidato);

    default Especificacao<T> e(Especificacao<? super T> outra) {
        return candidato -> violacao(candidato).or(() -> outra.violacao(candidato));
    }

    default void exigir(T candidato) {
        violacao(candidato).ifPresent(motivo -> {
            throw new RegraVioladaException(motivo);
        });
    }

    /** Atalho para as regras simples: viola com {@code motivo} quando {@code condicao} é falsa. */
    static <T> Especificacao<T> exige(Predicate<? super T> condicao, String motivo) {
        return candidato -> condicao.test(candidato) ? Optional.empty() : Optional.of(motivo);
    }
}
