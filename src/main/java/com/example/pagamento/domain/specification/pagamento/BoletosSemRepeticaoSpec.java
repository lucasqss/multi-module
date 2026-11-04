package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.BoletoRepetidoNoPagamentoException;
import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Seção de boletos: o mesmo boleto não entra duas vezes. */
public class BoletosSemRepeticaoSpec extends RegraNegocio<BoletosDoPagamento> {

    @Override
    public boolean isSatisfiedBy(BoletosDoPagamento s) {
        return !s.possuiRepetidos();
    }

    @Override
    protected BoletoRepetidoNoPagamentoException violacao(BoletosDoPagamento s) {
        return new BoletoRepetidoNoPagamentoException();
    }
}
