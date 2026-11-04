package com.example.pagamento.domain.state;

/** O banco recusou (saldo, conta...): pode ser reenviado ou cancelado. */
public final class EstadoRecusado extends EstadoPagamento {

    static final EstadoRecusado INSTANCIA = new EstadoRecusado();

    private EstadoRecusado() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.RECUSADO;
    }

    @Override
    public EstadoPagamento reenviarAoBanco() {
        return EstadoEmProcessamento.INSTANCIA;
    }

    @Override
    public EstadoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }
}
