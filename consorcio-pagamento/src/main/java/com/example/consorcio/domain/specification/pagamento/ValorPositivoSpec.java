package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** O valor do pagamento precisa ser maior que zero. */
public class ValorPositivoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return s.dados().valor().ehPositivo();
    }

    @Override
    public String codigo() {
        return "VALOR_NAO_POSITIVO";
    }

    @Override
    public String mensagem(SolicitacaoPagamento s) {
        return "Valor do pagamento deve ser maior que zero";
    }
}
