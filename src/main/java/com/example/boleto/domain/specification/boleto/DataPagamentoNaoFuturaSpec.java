package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.DataPagamentoFuturaException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Conformidade: data do pagamento informada não pode estar no futuro (dado suspeito: vai para análise). */
public class DataPagamentoNaoFuturaSpec extends RegraNegocio<RecebimentoPagamento> {

    @Override
    public boolean isSatisfiedBy(RecebimentoPagamento s) {
        return !s.pagamento().data().isAfter(s.hoje());
    }

    @Override
    protected DataPagamentoFuturaException violacao(RecebimentoPagamento s) {
        return new DataPagamentoFuturaException(s.pagamento().data());
    }
}
