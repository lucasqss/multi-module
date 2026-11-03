package com.example.consorcio.domain.specification;

import com.example.consorcio.domain.exception.RegraNegocioException;
import com.example.consorcio.domain.exception.Violacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conjunto ordenado de regras avaliadas JUNTAS: acumula todas as violações
 * e lança uma única {@link RegraNegocioException} com a lista completa.
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

    public List<Violacao> avaliar(T candidato) {
        return regras.stream()
                .map(regra -> regra.avaliar(candidato))
                .flatMap(Optional::stream)
                .toList();
    }

    public void validar(T candidato) {
        var violacoes = avaliar(candidato);
        if (!violacoes.isEmpty()) {
            throw new RegraNegocioException(violacoes);
        }
    }
}
