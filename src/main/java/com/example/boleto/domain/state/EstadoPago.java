package com.example.boleto.domain.state;


/**
 * Estado final: quitado. Um segundo pagamento (outra transação) para um boleto já pago é recusado
 * aqui (409) e fica para a conciliação manual, fora do escopo deste exemplo.
 */
public final class EstadoPago extends EstadoBoleto {

    static final EstadoPago INSTANCIA = new EstadoPago();

    private EstadoPago() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.PAGO;
    }
}
