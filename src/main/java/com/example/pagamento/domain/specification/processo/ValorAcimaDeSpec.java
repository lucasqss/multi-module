package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.specification.Specification;
import com.example.pagamento.domain.valueobject.Dinheiro;

/** Bloco de construção: "o valor do processo passa de X". */
public record ValorAcimaDeSpec(Dinheiro limite) implements Specification<AprovacaoProcesso> {

    @Override
    public boolean isSatisfiedBy(AprovacaoProcesso s) {
        return s.valorTotal().maiorQue(limite);
    }
}
