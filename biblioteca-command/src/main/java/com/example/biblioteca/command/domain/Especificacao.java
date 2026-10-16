package com.example.biblioteca.command.domain;

import java.util.Optional;

/**
 * Regra de negócio sobre um candidato. Devolve o motivo quando a regra é violada, para que regras com
 * vários motivos possíveis (ex.: leitor suspenso OU no limite) digam exatamente qual falhou.
 */
@FunctionalInterface
public interface Especificacao<T> {

    Optional<String> violacao(T candidato);

    default Especificacao<T> e(Especificacao<T> outra) {
        return candidato -> violacao(candidato).or(() -> outra.violacao(candidato));
    }

    default void exigir(T candidato) {
        violacao(candidato).ifPresent(motivo -> {
            throw new RegraVioladaException(motivo);
        });
    }
}
