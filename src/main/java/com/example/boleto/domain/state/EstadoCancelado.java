package com.example.boleto.domain.state;

import com.example.boleto.domain.enums.StatusBoleto;

/** Estado final: boleto cancelado. Nenhuma ação permitida. */
public final class EstadoCancelado extends EstadoBoleto {

    static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.CANCELADO;
    }
}
