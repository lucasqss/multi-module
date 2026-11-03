package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.exception.Violacao;

import java.util.Optional;
import java.util.function.Function;

/**
 * Specification que representa uma REGRA DE NEGÓCIO: além de dizer se o candidato satisfaz,
 * sabe o código e a mensagem do erro quando não satisfaz.
 *
 * <p>Duas formas de criar:
 * <ul>
 *   <li>Classe própria (regra com lógica não trivial), estendendo esta classe;</li>
 *   <li>{@link #de(String, Specification, Function)}, compondo specifications menores.</li>
 * </ul>
 */
public abstract class RegraNegocio<T> implements Specification<T> {

    public abstract String codigo();

    public abstract String mensagem(T candidato);

    public final Optional<Violacao> avaliar(T candidato) {
        return isSatisfiedBy(candidato)
                ? Optional.empty()
                : Optional.of(new Violacao(codigo(), mensagem(candidato)));
    }

    public static <T> RegraNegocio<T> de(String codigo, Specification<T> specification,
                                         Function<? super T, String> mensagem) {
        return new RegraNegocio<>() {
            @Override
            public boolean isSatisfiedBy(T candidato) {
                return specification.isSatisfiedBy(candidato);
            }

            @Override
            public String codigo() {
                return codigo;
            }

            @Override
            public String mensagem(T candidato) {
                return mensagem.apply(candidato);
            }
        };
    }
}
