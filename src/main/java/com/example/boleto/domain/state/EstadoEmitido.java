package com.example.boleto.domain.state;


/**
 * Recém-emitido, o pagador ainda não foi avisado: é o único estado em que dá para
 * alterar ou excluir (ninguém de fora conhece este boleto ainda).
 */
public final class EstadoEmitido extends EstadoBoleto {

    static final EstadoEmitido INSTANCIA = new EstadoEmitido();

    private EstadoEmitido() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.EMITIDO;
    }

    @Override
    public EstadoBoleto notificar() {
        return EstadoNotificado.INSTANCIA;
    }

    /** O pagador pode pagar mesmo sem ter recebido a notificação (ex.: recebeu a linha digitável por outro meio). */
    @Override
    public EstadoBoleto receberPagamento() {
        return EstadoPago.INSTANCIA;
    }

    @Override
    public EstadoBoleto cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    @Override
    public void validarAlteracao() {
        // permitido
    }

    @Override
    public void validarExclusao() {
        // permitido
    }

    @Override
    public boolean emAberto() {
        return true;
    }
}
