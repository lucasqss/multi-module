package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.Specification;
import com.example.consorcio.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor é no máximo X". */
public record ValorAteSpec(Dinheiro maximo) implements Specification<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.dados().valor().maiorQue(maximo);
    }
}
