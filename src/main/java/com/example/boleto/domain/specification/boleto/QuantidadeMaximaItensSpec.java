package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.QuantidadeMaximaItensExcedidaException;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.specification.RegraNegocio;

/** Seção de itens: no máximo 20 itens (regra fake). */
public class QuantidadeMaximaItensSpec extends RegraNegocio<ItensBoleto> {

    public static final int MAXIMO = 20;

    @Override
    public boolean isSatisfiedBy(ItensBoleto s) {
        return s.quantidade() <= MAXIMO;
    }

    @Override
    protected QuantidadeMaximaItensExcedidaException violacao(ItensBoleto s) {
        return new QuantidadeMaximaItensExcedidaException(MAXIMO);
    }
}
