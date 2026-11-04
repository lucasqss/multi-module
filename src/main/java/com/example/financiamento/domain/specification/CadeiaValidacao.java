package com.example.financiamento.domain.specification;

import java.util.function.Predicate;

/**
 * Chain of Responsibility para validação: uma sequência de ELOS. Cada elo decide se SE APLICA ao
 * candidato (ex.: só para um tipo de produto); se sim, valida com o seu {@link ValidadorRegras};
 * depois passa adiante para o próximo elo.
 *
 * <p>Serve para regras que VARIAM por tipo de produto sem {@code if}/{@code switch} no agregado:
 * para um tipo novo, acrescente um elo.
 *
 * <pre>
 * CadeiaValidacao.comecandoPor(ValidadorRegras.de(...))        // sempre
 *         .depois(s -> s.tipo() == A, ValidadorRegras.de(...)) // só para A
 *         .depois(s -> s.tipo() == B, ValidadorRegras.de(...)) // só para B
 * </pre>
 */
public final class CadeiaValidacao<T> {

    private final Predicate<? super T> aplicaA;
    private final ValidadorRegras<T> regras;
    private final CadeiaValidacao<T> anterior;

    private CadeiaValidacao(Predicate<? super T> aplicaA, ValidadorRegras<T> regras, CadeiaValidacao<T> anterior) {
        this.aplicaA = aplicaA;
        this.regras = regras;
        this.anterior = anterior;
    }

    /** Primeiro elo: vale para todo candidato. */
    public static <T> CadeiaValidacao<T> comecandoPor(ValidadorRegras<T> regras) {
        return new CadeiaValidacao<>(candidato -> true, regras, null);
    }

    /** Próximo elo da cadeia, aplicado só quando {@code aplicaA} é verdadeiro. */
    public CadeiaValidacao<T> depois(Predicate<? super T> aplicaA, ValidadorRegras<T> regras) {
        return new CadeiaValidacao<>(aplicaA, regras, this);
    }

    /** Percorre os elos na ordem em que foram declarados; para na primeira regra violada (fail-fast). */
    public void validar(T candidato) {
        if (anterior != null) {
            anterior.validar(candidato);
        }
        if (aplicaA.test(candidato)) {
            regras.validar(candidato);
        }
    }
}
