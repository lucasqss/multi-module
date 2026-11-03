package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.ItemComValorZeradoException;
import com.example.boleto.domain.valueobject.ItensBoleto;
import com.example.boleto.domain.specification.RegraNegocio;

/** Seção de itens: todo item precisa ter valor maior que zero. */
public class ItensComValorPositivoSpec extends RegraNegocio<ItensBoleto> {

    @Override
    public boolean isSatisfiedBy(ItensBoleto s) {
        return s.itens().stream().allMatch(i -> i.valor().ehPositivo());
    }

    @Override
    protected ItemComValorZeradoException violacao(ItensBoleto s) {
        return new ItemComValorZeradoException(s.itens().stream().filter(i -> !i.valor().ehPositivo()).findFirst().orElseThrow().descricao());
    }
}
