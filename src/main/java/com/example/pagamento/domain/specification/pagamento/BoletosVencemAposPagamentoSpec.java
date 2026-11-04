package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.exception.BoletoVenceAntesDoPagamentoException;
import com.example.pagamento.domain.valueobject.BoletoAPagar;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Nenhum boleto pode vencer antes da data de pagamento (fake: sem juros/multa). */
public class BoletosVencemAposPagamentoSpec extends RegraNegocio<SubmissaoPagamento> {

    private static BoletoAPagar primeiroVencido(SubmissaoPagamento s) {
        return s.data().flatMap(data -> s.boletos().boletos().stream()
                .filter(b -> b.vencimento().isBefore(data)).findFirst()).orElse(null);
    }

    @Override
    public boolean isSatisfiedBy(SubmissaoPagamento s) {
        return primeiroVencido(s) == null;
    }

    @Override
    protected BoletoVenceAntesDoPagamentoException violacao(SubmissaoPagamento s) {
        return new BoletoVenceAntesDoPagamentoException(primeiroVencido(s).beneficiario(), primeiroVencido(s).vencimento());
    }
}
