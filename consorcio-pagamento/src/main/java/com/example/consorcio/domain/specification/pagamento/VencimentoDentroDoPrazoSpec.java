package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Vencimento limitado a N dias a partir de hoje (N vem de configuração). */
public class VencimentoDentroDoPrazoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.dados().dataVencimento().isAfter(s.hoje().plusDays(s.prazoMaximoVencimentoDias()));
    }

    @Override
    public String codigo() {
        return "VENCIMENTO_ALEM_DO_PRAZO";
    }

    @Override
    public String mensagem(SolicitacaoPagamento s) {
        return "Data de vencimento deve ser em até %d dias".formatted(s.prazoMaximoVencimentoDias());
    }
}
