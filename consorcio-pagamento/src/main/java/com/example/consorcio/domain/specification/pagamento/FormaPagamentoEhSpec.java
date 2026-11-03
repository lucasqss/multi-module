package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.Specification;

/** Bloco de construção: "a forma de pagamento é X". */
public record FormaPagamentoEhSpec(FormaPagamento forma) implements Specification<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.dados().formaPagamento() == forma;
    }
}
