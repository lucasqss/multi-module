package com.example.boleto.domain.state;


/** Estado final: pagamento recebido. Nenhuma ação permitida. */
public final class EstadoPago extends EstadoBoleto {

    static final EstadoPago INSTANCIA = new EstadoPago();

    private EstadoPago() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.PAGO;
    }
}
