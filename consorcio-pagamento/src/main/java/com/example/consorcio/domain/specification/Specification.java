package com.example.consorcio.domain.specification;

/** Regra de negócio isolada, nomeada e reutilizável. */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidato);

    /** Código de erro usado quando a regra não é satisfeita. */
    String codigo();

    /** Mensagem amigável quando a regra não é satisfeita. */
    String mensagem(T candidato);
}
