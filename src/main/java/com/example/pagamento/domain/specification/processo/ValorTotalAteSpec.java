package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.specification.Specification;
import com.example.pagamento.domain.valueobject.Dinheiro;

/** Bloco de construção: "a soma dos boletos é no máximo X". */
public record ValorTotalAteSpec(Dinheiro maximo) implements Specification<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return !s.boletos().total().maiorQue(maximo);
    }
}
