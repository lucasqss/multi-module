package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Enviado à liquidação: congelado; só aguarda o retorno (confirmação ou rejeição). */
public final class EstadoEmProcessamento extends EstadoProcessoPagamento {

    static final EstadoEmProcessamento INSTANCIA = new EstadoEmProcessamento();

    private EstadoEmProcessamento() {
    }

    @Override
    public StatusProcessoPagamento status() {
        return StatusProcessoPagamento.EM_PROCESSAMENTO;
    }

    @Override
    public EstadoProcessoPagamento confirmar() {
        return EstadoConfirmado.INSTANCIA;
    }

    @Override
    public EstadoProcessoPagamento rejeitar() {
        return EstadoRejeitado.INSTANCIA;
    }

    @Override
    public boolean emAndamento() {
        return true;
    }
}
