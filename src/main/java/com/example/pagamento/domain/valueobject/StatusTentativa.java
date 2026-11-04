package com.example.pagamento.domain.valueobject;

/** Situação de UMA tentativa de pagamento no banco (não confundir com o status do processo). */
public enum StatusTentativa {
    ENVIADA,
    LIQUIDADA,
    RECUSADA;

    /** Ainda aguardando o retorno do banco. */
    public boolean aberta() {
        return this == ENVIADA;
    }
}
