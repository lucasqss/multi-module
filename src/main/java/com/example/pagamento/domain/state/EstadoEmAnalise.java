package com.example.pagamento.domain.state;

/**
 * Um fato externo fugiu do esperado (valor liquidado divergente, boleto cancelado após o envio). Aguarda
 * decisão humana; nesta versão do exemplo, só sai por cancelamento (o estorno é tratado fora).
 */
public final class EstadoEmAnalise extends EstadoProcesso {

    static final EstadoEmAnalise INSTANCIA = new EstadoEmAnalise();

    private EstadoEmAnalise() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.EM_ANALISE;
    }

    /** Novos fatos são registrados, mas o processo continua aguardando a análise. */
    @Override
    public EstadoProcesso receberRetornoBanco(boolean liquidado, boolean conforme) {
        return this;
    }

    @Override
    public EstadoProcesso boletoCanceladoNoEmissor() {
        return this;
    }

    @Override
    public boolean emAnalise() {
        return true;
    }

    @Override
    public EstadoProcesso cancelar() {
        return EstadoCancelado.INSTANCIA;
    }
}
