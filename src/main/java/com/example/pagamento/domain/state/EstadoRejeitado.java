package com.example.pagamento.domain.state;

/** Um aprovador rejeitou: pode ser reaberto para correção (volta a EM_EDICAO, sem as aprovações) ou cancelado. */
public final class EstadoRejeitado extends EstadoProcesso {

    static final EstadoRejeitado INSTANCIA = new EstadoRejeitado();

    private EstadoRejeitado() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.REJEITADO;
    }

    @Override
    public EstadoProcesso reabrir() {
        return EstadoEmEdicao.INSTANCIA;
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
