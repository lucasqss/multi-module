package com.example.pagamento.domain.state;

/** Rascunho: seções (boletos, conta, data) salvas por partes; pode estar incompleto e pode ser excluído. */
public final class EstadoEmEdicao extends EstadoProcesso {

    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();

    private EstadoEmEdicao() {
    }

    @Override
    public StatusProcesso status() {
        return StatusProcesso.EM_EDICAO;
    }

    @Override
    public EstadoProcesso submeter() {
        return EstadoAguardandoAprovacao.INSTANCIA;
    }

    @Override
    public void validarEdicao() {
        // permitido
    }

    @Override
    public void validarExclusao() {
        // permitido
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
