package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.VencimentoNoPassadoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Vencimento não pode ser anterior a hoje. */
public class VencimentoNaoRetroativoSpec extends RegraNegocio<EmissaoBoleto> {

    @Override
    public boolean isSatisfiedBy(EmissaoBoleto s) {
        return !s.dataVencimento().isBefore(s.hoje());
    }

    @Override
    protected VencimentoNoPassadoException violacao(EmissaoBoleto s) {
        return new VencimentoNoPassadoException(s.dataVencimento());
    }
}
