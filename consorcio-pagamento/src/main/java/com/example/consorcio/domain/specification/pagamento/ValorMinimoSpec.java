package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.Specification;
import com.example.consorcio.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor é no mínimo X". */
public record ValorMinimoSpec(Dinheiro minimo) implements Specification<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !minimo.maiorQue(s.dados().valor());
    }
}
