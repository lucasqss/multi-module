package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.specification.Specification;
import com.example.boleto.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor do boleto é no máximo X". */
public record ValorAteSpec(Dinheiro maximo) implements Specification<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return !s.dados().valor().maiorQue(maximo);
    }
}
