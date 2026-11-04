package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.exception.QuantidadeMaximaBoletosExcedidaException;
import com.example.pagamento.domain.valueobject.BoletosDoProcesso;
import com.example.pagamento.domain.specification.RegraNegocio;

/** Seção de boletos: no máximo 20 por processo (regra fake). */
public class QuantidadeMaximaBoletosSpec extends RegraNegocio<BoletosDoProcesso> {

    public static final int MAXIMO = 20;

    @Override
    public boolean isSatisfiedBy(BoletosDoProcesso s) {
        return s.quantidade() <= MAXIMO;
    }

    @Override
    protected QuantidadeMaximaBoletosExcedidaException violacao(BoletosDoProcesso s) {
        return new QuantidadeMaximaBoletosExcedidaException(MAXIMO);
    }
}
