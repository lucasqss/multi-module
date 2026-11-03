package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Estado final: processo cancelado. Nenhuma ação permitida. */
public final class EstadoCancelado extends EstadoProcessoPagamento {

    static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public StatusProcessoPagamento status() {
        return StatusProcessoPagamento.CANCELADO;
    }

    @Override
    public boolean finalizado() {
        return true;
    }
}
