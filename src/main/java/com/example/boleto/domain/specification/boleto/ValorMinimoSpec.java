package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;
import com.example.boleto.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor do boleto é no mínimo X". */
public record ValorMinimoSpec(Dinheiro minimo) implements Specification<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return !minimo.maiorQue(s.valor());
    }
}
