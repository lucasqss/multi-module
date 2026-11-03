package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.CotaNaoAceitaPagamentoException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Cota cancelada/encerrada não recebe pagamento. */
public class CotaAceitaPagamentoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.cota().aceitaPagamento();
    }

    @Override
    protected CotaNaoAceitaPagamentoException violacao(SolicitacaoPagamento s) {
        return new CotaNaoAceitaPagamentoException(s.cota().identificador(), s.cota().situacao());
    }
}
