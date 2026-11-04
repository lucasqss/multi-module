package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.ValorLiquidadoDivergenteException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Conformidade (fato): se liquidou, foi exatamente o total do pagamento. Recusa não é avaliada aqui. */
public class ValorLiquidadoIgualAoTotalSpec extends RegraNegocio<RetornoPagamento> {

    @Override
    public boolean isSatisfiedBy(RetornoPagamento s) {
        return !s.retorno().liquidado() || s.retorno().valorLiquidado().equals(s.valorTotal());
    }

    @Override
    protected ValorLiquidadoDivergenteException violacao(RetornoPagamento s) {
        return new ValorLiquidadoDivergenteException(s.retorno().valorLiquidado(), s.valorTotal());
    }
}
