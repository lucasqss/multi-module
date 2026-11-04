package com.example.pagamento.domain.state;

/** Completo e validado: aguarda o envio ao banco (na vida real, um job na data de pagamento). */
public final class EstadoAgendado extends EstadoPagamento {

    static final EstadoAgendado INSTANCIA = new EstadoAgendado();

    private EstadoAgendado() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.AGENDADO;
    }

    @Override
    public EstadoPagamento enviarAoBanco() {
        return EstadoEmProcessamento.INSTANCIA;
    }

    @Override
    public EstadoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }
}
