package com.example.consorcio.domain.specification.pagamento;

import com.example.consorcio.domain.exception.CotaComProcessoEmAndamentoException;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.specification.RegraNegocio;

/**
 * Só pode haver um processo em andamento por cota.
 * O FATO ("existe processo em andamento?") é consultado pelo use case e entregue na solicitação;
 * a REGRA ("não pode existir") mora aqui.
 */
public class CotaSemProcessoEmAndamentoSpec extends RegraNegocio<SolicitacaoPagamento> {

    @Override
    public boolean isSatisfiedBy(SolicitacaoPagamento s) {
        return !s.cotaPossuiProcessoEmAndamento();
    }

    @Override
    protected CotaComProcessoEmAndamentoException violacao(SolicitacaoPagamento s) {
        return new CotaComProcessoEmAndamentoException(s.cota().identificador());
    }
}
