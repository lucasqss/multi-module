package com.example.consorcio.domain.enums;

public enum FormaPagamento {
    BOLETO,
    PIX,
    DEBITO_EM_CONTA;

    /** A regra "débito exige conta" mora aqui, perto do dado que ela descreve. */
    public boolean exigeContaDebito() {
        return this == DEBITO_EM_CONTA;
    }
}
