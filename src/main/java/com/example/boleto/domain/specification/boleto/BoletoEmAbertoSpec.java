package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.BoletoNaoEstaEmAbertoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Conformidade: o boleto pago precisa estar em aberto (pagar boleto cancelado vai para análise). */
public class BoletoEmAbertoSpec extends RegraNegocio<RecebimentoPagamento> {

    @Override
    public boolean isSatisfiedBy(RecebimentoPagamento s) {
        return s.boletoEmAberto();
    }

    @Override
    protected BoletoNaoEstaEmAbertoException violacao(RecebimentoPagamento s) {
        return new BoletoNaoEstaEmAbertoException(s.statusBoleto());
    }
}
