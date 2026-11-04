package com.example.pagamento.domain.state;

/** Completo e submetido: aguarda as aprovações exigidas pela alçada. */
public final class EstadoAguardandoAprovacao extends EstadoProcesso {

    static final EstadoAguardandoAprovacao INSTANCIA = new EstadoAguardandoAprovacao();

    private EstadoAguardandoAprovacao() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.AGUARDANDO_APROVACAO;
    }

    /** Ainda falta aprovação? Continua aqui (auto-transição). Alçada atingida? Aprovado. */
    @Override
    public EstadoProcesso registrarAprovacao(boolean alcadaAtingida) {
        return alcadaAtingida ? EstadoAprovado.INSTANCIA : this;
    }

    @Override
    public EstadoProcesso rejeitar() {
        return EstadoRejeitado.INSTANCIA;
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
