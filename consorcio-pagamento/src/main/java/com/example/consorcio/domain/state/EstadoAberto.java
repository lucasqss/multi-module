package com.example.consorcio.domain.state;

import com.example.consorcio.domain.enums.StatusProcessoPagamento;

/** Recém-criado: pode ser alterado, excluído, enviado ou cancelado. */
public final class EstadoAberto extends EstadoProcessoPagamento {

    static final EstadoAberto INSTANCIA = new EstadoAberto();

    private EstadoAberto() {
    }

    @Override
    public StatusProcessoPagamento status() {
        return StatusProcessoPagamento.ABERTO;
    }

    @Override
    public EstadoProcessoPagamento enviarParaProcessamento() {
        return EstadoEmProcessamento.INSTANCIA;
    }

    @Override
    public EstadoProcessoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    @Override
    public void validarAlteracao() {
        // permitido
    }

    @Override
    public void validarExclusao() {
        // permitido: nunca saiu do nosso sistema
    }

    @Override
    public boolean emAndamento() {
        return true;
    }
}
