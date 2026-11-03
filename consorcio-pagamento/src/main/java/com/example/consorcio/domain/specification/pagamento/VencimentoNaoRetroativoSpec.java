package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/** Vencimento não pode ser anterior a hoje. */
public class VencimentoNaoRetroativoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.dados().dataVencimento().isBefore(s.hoje());
    }

    @Override
    public String codigo() {
        return "VENCIMENTO_NO_PASSADO";
    }

    @Override
    public String mensagem(SolicitacaoPagamento s) {
        return "Data de vencimento %s não pode estar no passado".formatted(s.dados().dataVencimento());
    }
}
