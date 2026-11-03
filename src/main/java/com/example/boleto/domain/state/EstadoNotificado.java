package com.example.boleto.domain.state;

import com.example.boleto.domain.enums.StatusBoleto;

/**
 * O pagador já recebeu o boleto: não pode mais ser alterado nem excluído
 * (para mudar algo, cancele e emita outro). Aguarda o pagamento.
 */
public final class EstadoNotificado extends EstadoBoleto {

    static final EstadoNotificado INSTANCIA = new EstadoNotificado();

    private EstadoNotificado() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.NOTIFICADO;
    }

    /** Reenvio da notificação: transição para o PRÓPRIO estado. */
    @Override
    public EstadoBoleto notificar() {
        return this;
    }

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
