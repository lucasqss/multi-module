package com.example.pagamento.domain.state;

/** Estado final de sucesso. Nenhuma ação permitida. */
public final class EstadoPago extends EstadoPagamento {

    static final EstadoPago INSTANCIA = new EstadoPago();

    private EstadoPago() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.PAGO;
    }
}
