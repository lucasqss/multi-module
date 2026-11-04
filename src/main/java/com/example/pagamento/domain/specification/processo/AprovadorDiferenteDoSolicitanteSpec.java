package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.AprovadorIgualAoSolicitanteException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Segregação de funções: quem solicitou não aprova. */
public class AprovadorDiferenteDoSolicitanteSpec extends RegraNegocio<AprovacaoProcesso> {

    @Override
    public boolean isSatisfiedBy(AprovacaoProcesso s) {
        return !s.novoAprovador().equals(s.solicitante());
    }

    @Override
    protected AprovadorIgualAoSolicitanteException violacao(AprovacaoProcesso s) {
        return new AprovadorIgualAoSolicitanteException();
    }
}
