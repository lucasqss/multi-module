package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.BoletoRepetidoNoProcessoException;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Seção de boletos: o mesmo boleto não entra duas vezes. */
public class BoletosSemRepeticaoSpec extends RegraNegocio<BoletosDoProcesso> {

    @Override
    public boolean isSatisfiedBy(BoletosDoProcesso s) {
        return !s.possuiRepetidos();
    }

    @Override
    protected BoletoRepetidoNoProcessoException violacao(BoletosDoProcesso s) {
        return new BoletoRepetidoNoProcessoException();
    }
}
