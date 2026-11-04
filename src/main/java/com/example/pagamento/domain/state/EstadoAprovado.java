package com.example.pagamento.domain.state;

/** Aprovado: pronto para ser enviado ao banco (por um job/agendamento ou manualmente). */
public final class EstadoAprovado extends EstadoProcesso {

    static final EstadoAprovado INSTANCIA = new EstadoAprovado();

    private EstadoAprovado() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.APROVADO;
    }

    @Override
    public EstadoProcesso enviarParaPagamento() {
        return EstadoEmProcessamento.INSTANCIA;
    }

    @Override
    public EstadoProcesso cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    /** Ainda não foi ao banco: sem o boleto, o processo perde o sentido. */
    @Override
    public EstadoProcesso boletoCanceladoNoEmissor() {
        return EstadoCancelado.INSTANCIA;
    }
}
