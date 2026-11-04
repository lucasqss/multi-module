package com.example.pagamento.domain.state;

/** Rascunho: seções salvas por partes, pode estar incompleto e pode ser excluído. */
public final class EstadoEmEdicao extends EstadoPagamento {

    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();

    private EstadoEmEdicao() {
    }

    @Override
    public StatusPagamento status() {
        return StatusPagamento.EM_EDICAO;
    }

    @Override
    public EstadoPagamento submeter() {
        return EstadoAgendado.INSTANCIA;
    }

    @Override
    public EstadoPagamento cancelar() {
        return EstadoCancelado.INSTANCIA;
    }

    @Override
    public void validarEdicao() {
        // permitido
    }

    @Override
    public void validarExclusao() {
        // permitido
    }
}
