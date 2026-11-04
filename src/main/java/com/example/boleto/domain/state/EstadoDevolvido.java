package com.example.boleto.domain.state;

/** Estado final: o pagamento divergente foi devolvido ao pagador pelo sistema de pagamentos. */
public final class EstadoDevolvido extends EstadoBoleto {

    static final EstadoDevolvido INSTANCIA = new EstadoDevolvido();

    private EstadoDevolvido() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.DEVOLVIDO;
    }
}
