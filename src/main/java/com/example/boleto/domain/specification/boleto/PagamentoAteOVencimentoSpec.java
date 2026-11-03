package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.BoletoVencidoException;
import com.example.boleto.domain.model.RecebimentoPagamento;
import com.example.boleto.domain.specification.RegraNegocio;

/** Regra fake: boleto vencido não aceita pagamento (não há cálculo de juros/multa). */
public class PagamentoAteOVencimentoSpec extends RegraNegocio<RecebimentoPagamento> {

    @Override
    public boolean isSatisfiedBy(RecebimentoPagamento s) {
        return !s.pagamento().data().isAfter(s.dataVencimento());
    }

    @Override
    protected BoletoVencidoException violacao(RecebimentoPagamento s) {
        return new BoletoVencidoException(s.dataVencimento(), s.pagamento().data());
    }
}
