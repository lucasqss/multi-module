package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Estado final: pagamento liquidado. Nenhuma ação permitida. */
public final class EstadoConfirmado extends EstadoProcessoPagamento {

    static final EstadoConfirmado INSTANCIA = new EstadoConfirmado();

    private EstadoConfirmado() {
    }

    @Override
    public StatusProcessoPagamento status() {
        return StatusProcessoPagamento.CONFIRMADO;
    }

    @Override
    public boolean finalizado() {
        return true;
    }
}
