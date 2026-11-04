package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.exception.PrazoInvalidoException;
import com.example.financiamento.domain.specification.RegraNegocio;

/** Prazo dentro da faixa do tipo. Parametrizada: uma classe serve aos dois elos da cadeia. */
public class PrazoSpec extends RegraNegocio<SubmissaoFinanciamento> {

    private final int minimo;
    private final int maximo;

    public PrazoSpec(int minimo, int maximo) {
        this.minimo = minimo;
        this.maximo = maximo;
    }

    @Override
    public boolean isSatisfiedBy(SubmissaoFinanciamento s) {
        return s.prazoMeses() >= minimo && s.prazoMeses() <= maximo;
    }

    @Override
    protected PrazoInvalidoException violacao(SubmissaoFinanciamento s) {
        return new PrazoInvalidoException(s.tipo(), minimo, maximo);
    }
}
