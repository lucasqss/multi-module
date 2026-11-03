package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.ValorPagoDivergenteException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Regra fake: não aceita pagamento parcial nem a maior. */
public class ValorPagoIgualAoBoletoSpec extends RegraNegocio<RecebimentoPagamento> {

    @Override
    public boolean isSatisfiedBy(RecebimentoPagamento s) {
        return s.pagamento().valorPago().equals(s.valorBoleto());
    }

    @Override
    protected ValorPagoDivergenteException violacao(RecebimentoPagamento s) {
        return new ValorPagoDivergenteException(s.pagamento().valorPago(), s.valorBoleto());
    }
}
