package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.BoletoSemItensException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Completude: só se emite boleto com ao menos um item. */
public class BoletoComItensSpec extends RegraNegocio<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return !s.itens().vazio();
    }

    @Override
    protected BoletoSemItensException violacao(EmissaoBoleto s) {
        return new BoletoSemItensException();
    }
}
