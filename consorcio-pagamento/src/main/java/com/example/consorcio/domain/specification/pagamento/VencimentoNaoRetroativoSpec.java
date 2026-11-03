package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.VencimentoNoPassadoException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Vencimento não pode ser anterior a hoje. */
public class VencimentoNaoRetroativoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.dados().dataVencimento().isBefore(s.hoje());
    }

    @Override
    protected VencimentoNoPassadoException violacao(SolicitacaoPagamento s) {
        return new VencimentoNoPassadoException(s.dados().dataVencimento());
    }
}
