package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.QuantidadeDeBoletosInvalidaException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Quantidade de boletos dentro da faixa da modalidade. Parametrizada: uma classe serve às três. */
public class QuantidadeDeBoletosSpec extends RegraNegocio<SubmissaoPagamento> {

    private final int minimo;
    private final int maximo;

    public QuantidadeDeBoletosSpec(int minimo, int maximo) {
        this.minimo = minimo;
        this.maximo = maximo;
    }

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        var quantidade = s.boletos().quantidade();
        return quantidade >= minimo && quantidade <= maximo;
    }

    @Override
    protected QuantidadeDeBoletosInvalidaException violacao(SubmissaoPagamento s) {
        return new QuantidadeDeBoletosInvalidaException(s.modalidade(), minimo, maximo);
    }
}
