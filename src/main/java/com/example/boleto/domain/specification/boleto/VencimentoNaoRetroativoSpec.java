package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.VencimentoNoPassadoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Vencimento não pode ser anterior a hoje. */
public class VencimentoNaoRetroativoSpec extends RegraNegocio<SolicitacaoBoleto> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return !s.dados().dataVencimento().isBefore(s.hoje());
    }

    @Override
    protected VencimentoNoPassadoException violacao(SolicitacaoBoleto s) {
        return new VencimentoNoPassadoException(s.dados().dataVencimento());
    }
}
