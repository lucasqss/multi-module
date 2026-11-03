package com.example.boleto.domain.specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Conjunto ORDENADO de regras. Avalia na ordem e para na primeira violada (fail-fast),
 * lançando a exceção específica dela. A ordem importa: coloque primeiro as regras mais "fundamentais"
 * (ex.: limites de valor) e depois as de detalhe.
 */
public final class ValidadorRegras<T> {

    private final List<RegraNegocio<? super T>> regras;

    private ValidadorRegras(List<RegraNegocio<? super T>> regras) {
        this.regras = List.copyOf(regras);
    }

    @SafeVarargs
    public static <T> ValidadorRegras<T> de(RegraNegocio<? super T>... regras) {
        return new ValidadorRegras<T>(List.of(regras));
    }

    /** Novo validador com as regras deste + as adicionais (permite reaproveitar conjuntos). */
    @SafeVarargs
    public final ValidadorRegras<T> mais(RegraNegocio<? super T>... adicionais) {
        var todas = new ArrayList<RegraNegocio<? super T>>(regras);
        todas.addAll(List.of(adicionais));
        return new ValidadorRegras<>(todas);
    }

    public void validar(T candidato) {
        regras.forEach(regra -> regra.exigir(candidato));
    }
}
