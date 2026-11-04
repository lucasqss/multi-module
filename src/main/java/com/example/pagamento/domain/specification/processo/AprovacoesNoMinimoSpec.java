package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.specification.Specification;

/** Bloco de construção: "com a aprovação atual, há pelo menos N aprovações". */
public record AprovacoesNoMinimoSpec(int minimo) implements Specification<AprovacaoProcesso> {

    @Override
    public boolean isSatisfiedBy(AprovacaoProcesso s) {
        return s.totalDeAprovacoes() >= minimo;
    }
}
