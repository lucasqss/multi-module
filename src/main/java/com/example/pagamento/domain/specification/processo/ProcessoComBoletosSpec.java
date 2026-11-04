package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.ProcessoSemBoletosException;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Completude: ao menos um boleto. */
public class ProcessoComBoletosSpec extends RegraNegocio<SubmissaoProcesso> {

    @Override
    public boolean isSatisfiedBy(SubmissaoProcesso s) {
        return !s.boletos().vazio();
    }

    @Override
    protected ProcessoSemBoletosException violacao(SubmissaoProcesso s) {
        return new ProcessoSemBoletosException();
    }
}
