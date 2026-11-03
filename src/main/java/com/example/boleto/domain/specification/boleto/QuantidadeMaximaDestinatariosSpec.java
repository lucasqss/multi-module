package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.QuantidadeMaximaDestinatariosExcedidaException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Seção de destinatários: no máximo 5 e-mails extras (regra fake). */
public class QuantidadeMaximaDestinatariosSpec extends RegraNegocio<DestinatariosDoBoleto> {

    public static final int MAXIMO = 5;

    @Override
    public boolean isSatisfiedBy(DestinatariosDoBoleto s) {
        return s.destinatarios().quantidade() <= MAXIMO;
    }

    @Override
    protected QuantidadeMaximaDestinatariosExcedidaException violacao(DestinatariosDoBoleto s) {
        return new QuantidadeMaximaDestinatariosExcedidaException(MAXIMO);
    }
}
