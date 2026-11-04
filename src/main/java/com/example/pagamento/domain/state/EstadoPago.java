package com.example.pagamento.domain.state;

/** Estado final de sucesso. Se o emissor cancelar o boleto depois, o fato vai para análise. */
public final class EstadoPago extends EstadoProcesso {

    static final EstadoPago INSTANCIA = new EstadoPago();

    private EstadoPago() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.PAGO;
    }

    @Override
    public EstadoProcesso boletoCanceladoNoEmissor() {
        return EstadoEmAnalise.INSTANCIA;
    }
}
