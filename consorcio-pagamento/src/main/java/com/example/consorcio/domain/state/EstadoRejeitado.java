package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Liquidação recusou: pode ser reaberto (para corrigir e reenviar) ou cancelado. */
public final class EstadoRejeitado extends EstadoProcessoPagamento {

    static final EstadoRejeitado INSTANCIA = new EstadoRejeitado();

    private EstadoRejeitado() {
    }

    @Override
    public StatusProcessoPagamento status() {
        return StatusProcessoPagamento.REJEITADO;
    }

    @Override
    public EstadoProcessoPagamento reabrir() {
        return EstadoAberto.INSTANCIA;
    }

    @Override
    public EstadoProcessoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    @Override
    public boolean emAndamento() {
        return true;
    }
}
