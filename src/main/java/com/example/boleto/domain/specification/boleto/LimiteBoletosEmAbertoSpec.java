package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.LimiteBoletosEmAbertoExcedidoException;
import com.example.boleto.domain.model.SolicitacaoBoleto;
import com.example.boleto.domain.specification.RegraNegocio;

/**
 * Um pagador pode ter no máximo N boletos em aberto (regra fake).
 * O FATO ("quantos boletos em aberto ele tem?") é consultado pelo use case e entregue na solicitação;
 * a REGRA ("no máximo N") mora aqui.
 */
public class LimiteBoletosEmAbertoSpec extends RegraNegocio<SolicitacaoBoleto> {

    public static final int LIMITE = 3;

    @Override
    public boolean isSatisfiedBy(SolicitacaoBoleto s) {
        return s.boletosEmAbertoDoPagador() < LIMITE;
    }

    @Override
    protected LimiteBoletosEmAbertoExcedidoException violacao(SolicitacaoBoleto s) {
        return new LimiteBoletosEmAbertoExcedidoException(s.dados().pagador().documento(), LIMITE);
    }
}
