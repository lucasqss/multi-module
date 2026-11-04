package com.example.pagamento.domain.state;

/** Estado final. Novos cancelamentos do emissor são ignorados (já está cancelado). */
public final class EstadoCancelado extends EstadoProcesso {

    static final EstadoCancelado INSTANCIA = new EstadoCancelado();

    private EstadoCancelado() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.CANCELADO;
    }

    @Override
    public EstadoProcesso boletoCanceladoNoEmissor() {
        return this;
    }
}
