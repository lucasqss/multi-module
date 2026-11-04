package com.example.pagamento.domain.state;

/**
 * O banco liquidou um valor diferente do total (um fato que não se recusa). Aguarda decisão humana;
 * neste exemplo, só sai por cancelamento (o acerto com o fornecedor é tratado fora).
 */
public final class EstadoEmAnalise extends EstadoPagamento {

    static final EstadoEmAnalise INSTANCIA = new EstadoEmAnalise();

    private EstadoEmAnalise() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.EM_ANALISE;
    }

    @Override
    public EstadoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }
}
