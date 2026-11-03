package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;
import com.example.boleto.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor do boleto é no mínimo X". */
public record ValorMinimoSpec(Dinheiro minimo) implements Specification<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return !minimo.maiorQue(s.dados().valor());
    }
}
