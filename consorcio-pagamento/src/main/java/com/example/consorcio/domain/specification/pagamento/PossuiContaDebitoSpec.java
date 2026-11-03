package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.Specification;

/** Bloco de construção: "foi informada conta para débito". */
public class PossuiContaDebitoSpec implements Specification<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.dados().conta().isPresent();
    }
}
