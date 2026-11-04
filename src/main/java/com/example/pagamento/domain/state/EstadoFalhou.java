package com.example.pagamento.domain.state;

/** O banco recusou a última tentativa (saldo, conta...): pode ser retentado ou cancelado. */
public final class EstadoFalhou extends EstadoProcesso {

    static final EstadoFalhou INSTANCIA = new EstadoFalhou();

    private EstadoFalhou() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.FALHOU;
    }

    @Override
    public EstadoProcesso retentarPagamento() {
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
