package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.AprovadorRepetidoException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** A mesma pessoa não aprova duas vezes. */
public class AprovadorNaoRepetidoSpec extends RegraNegocio<AprovacaoProcesso> {

    @Override
    public boolean isSatisfiedBy(AprovacaoProcesso s) {
        return !s.aprovadoresAnteriores().contains(s.novoAprovador());
    }

    @Override
    protected AprovadorRepetidoException violacao(AprovacaoProcesso s) {
        return new AprovadorRepetidoException(s.novoAprovador());
    }
}
