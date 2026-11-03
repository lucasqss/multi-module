package com.example.boleto.domain.state;


/**
 * Emitido (completo, com linha digitável), o pagador ainda não foi avisado.
 * Não aceita mais edição: para mudar algo, cancele e crie outro.
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
    public boolean emAberto() {
        return true;
    }
}
