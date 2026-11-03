package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.exception.DestinatarioRepetidoException;
import com.example.boleto.domain.specification.RegraNegocio;

/** Seção de destinatários: sem e-mails repetidos, nem o próprio e-mail do pagador. */
public class DestinatariosSemRepeticaoSpec extends RegraNegocio<DestinatariosDoBoleto> {

    @Override
    public boolean isSatisfiedBy(DestinatariosDoBoleto s) {
        return !s.destinatarios().possuiRepetidos() && !s.destinatarios().contem(s.pagador().email());
    }

    @Override
    protected DestinatarioRepetidoException violacao(DestinatariosDoBoleto s) {
        return new DestinatarioRepetidoException();
    }
}
