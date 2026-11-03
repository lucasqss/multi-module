package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.ValorExcedeSaldoDevedorException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Não se paga mais do que se deve. */
public class ValorDentroDoSaldoDevedorSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.cota().comportaPagamentoDe(s.dados().valor());
    }

    @Override
    protected ValorExcedeSaldoDevedorException violacao(SolicitacaoPagamento s) {
        return new ValorExcedeSaldoDevedorException(s.dados().valor(), s.cota().saldoDevedor());
    }
}
