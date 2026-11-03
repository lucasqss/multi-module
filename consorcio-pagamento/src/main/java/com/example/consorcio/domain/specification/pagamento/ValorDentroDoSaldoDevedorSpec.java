package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Não se paga mais do que se deve. */
public class ValorDentroDoSaldoDevedorSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.cota().comportaPagamentoDe(s.dados().valor());
    }

    @Override
    public String codigo() {
        return "VALOR_EXCEDE_SALDO_DEVEDOR";
    }

    @Override
    public String mensagem(SolicitacaoPagamento s) {
        return "Valor %s excede o saldo devedor da cota (%s)".formatted(s.dados().valor(), s.cota().saldoDevedor());
    }
}
