package com.example.pagamento.domain.specification;

import com.example.pagamento.domain.exception.RegraNegocioException;

import java.util.function.Function;

/**
 * Specification que representa uma REGRA DE NEGÓCIO: além de dizer se o candidato satisfaz,
 * sabe qual exceção ESPECÍFICA lançar quando não satisfaz.
 *
 * <p>Duas formas de criar:
 * <ul>
 *   <li>classe própria, estendendo esta (regra com lógica não trivial);</li>
 *   <li>{@link #de(Specification, Function)}, compondo specifications menores.</li>
 * </ul>
 */
public abstract class RegraNegocio<T> implements Specification<T> {

    /** A exceção específica desta regra, já com os dados do candidato na mensagem. */
    protected abstract RegraNegocioException violacao(T candidato);

    /** A exceção específica desta regra para o candidato, SEM lançar (para classificar fatos). */
    public final RegraNegocioException violacaoDe(T candidato) {
        return violacao(candidato);
    }

    /** Lança a exceção específica se o candidato não satisfizer a regra. */
    public final void exigir(T candidato) {
        if (!isSatisfiedBy(candidato)) {
            throw violacao(candidato);
        }
    }

    public static <T> RegraNegocio<T> de(Specification<T> specification,
                                         Function<? super T, ? extends RegraNegocioException> violacao) {
        return new RegraNegocio<>() {
            @Override
            public boolean isSatisfiedBy(T candidato) {
                return specification.isSatisfiedBy(candidato);
            }

            @Override
            protected RegraNegocioException violacao(T candidato) {
                return violacao.apply(candidato);
            }
        };
    }
}
