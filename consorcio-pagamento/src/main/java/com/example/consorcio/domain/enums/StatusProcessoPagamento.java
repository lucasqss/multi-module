package com.example.consorcio.domain.enums;

import java.util.Set;

/**
 * Status do processo com as transições permitidas.
 * (Para máquinas de estado maiores, migre para o pacote {@code domain.state} com o padrão State.)
 */
public enum StatusProcessoPagamento {
    ABERTO,
    CONFIRMADO,
    CANCELADO;

    public boolean podeTransicionarPara(StatusProcessoPagamento destino) {
        return switch (this) {
            case ABERTO -> Set.of(CONFIRMADO, CANCELADO).contains(destino);
            case CONFIRMADO, CANCELADO -> false;
        };
    }

    public boolean emAberto() {
        return this == ABERTO;
    }
}
