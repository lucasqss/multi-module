package com.example.consorcio.domain.enums;

public enum SituacaoCota {
    ATIVA,
    INADIMPLENTE,
    CANCELADA,
    ENCERRADA;

    /** Cota inadimplente pode pagar (é assim que ela se regulariza); cancelada/encerrada não. */
    public boolean aceitaPagamento() {
        return this == ATIVA || this == INADIMPLENTE;
    }
}
