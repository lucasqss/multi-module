package com.example.consorcio.domain.specification;

/**
 * Specification pattern: um predicado de negócio nomeado e COMPONÍVEL.
 *
 * <p>Use specifications "puras" (sem código de erro) como blocos de construção
 * e combine-as com {@link #and}, {@link #or} e {@link #not} para formar regras maiores.
 * Para virar uma regra que gera erro, embrulhe com {@link RegraNegocio}.
 */
@FunctionalInterface
public interface Specification<T> {

    boolean isSatisfiedBy(T candidato);

    default Specification<T> and(Specification<? super T> outra) {
        return candidato -> isSatisfiedBy(candidato) && outra.isSatisfiedBy(candidato);
    }

    default Specification<T> or(Specification<? super T> outra) {
        return candidato -> isSatisfiedBy(candidato) || outra.isSatisfiedBy(candidato);
    }

    default Specification<T> not() {
        return candidato -> !isSatisfiedBy(candidato);
    }

    /** Lê melhor em regras condicionais: {@code quando(ehPix).entao(valorAte5000)}. */
    static <T> Condicional<T> quando(Specification<T> condicao) {
        return consequencia -> condicao.not().or(consequencia);
    }

    @FunctionalInterface
    interface Condicional<T> {
        /** "Se a condição vale, então a consequência tem que valer" (implicação lógica). */
        Specification<T> entao(Specification<? super T> consequencia);
    }
}
