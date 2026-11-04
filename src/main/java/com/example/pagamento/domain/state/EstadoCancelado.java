package com.example.pagamento.domain.state;

/** Estado final. Nenhuma ação permitida. */
public final class EstadoCancelado extends EstadoPagamento {

    static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.CANCELADO;
    }
}
