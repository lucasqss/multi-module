package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;
import com.example.boleto.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor do boleto é no máximo X". */
public record ValorAteSpec(Dinheiro maximo) implements Specification<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return !s.valor().maiorQue(maximo);
    }
}
