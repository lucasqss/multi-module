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

    /** Conforme quita; divergente vai para análise (o dinheiro já entrou, não dá para recusar). */
    @Override
    public EstadoBoleto receberPagamento(boolean conforme) {
        return conforme ? EstadoPago.INSTANCIA : EstadoEmAnalise.INSTANCIA;
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
