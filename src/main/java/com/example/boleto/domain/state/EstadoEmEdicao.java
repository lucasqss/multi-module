package com.example.boleto.domain.state;

/**
 * Rascunho: as seções (vencimento, itens, destinatários) podem ser salvas por partes e o boleto
 * pode estar INCOMPLETO. Ninguém de fora o conhece, então também pode ser excluído.
 * Não é cobrável: não tem linha digitável e não conta no limite de boletos em aberto.
 */
public final class EstadoEmEdicao extends EstadoBoleto {

    static final EstadoEmEdicao INSTANCIA = new EstadoEmEdicao();

    private EstadoEmEdicao() {
    }

    @Override
    public StatusBoleto status() {
        return StatusBoleto.EM_EDICAO;
    }

    @Override
    public EstadoBoleto emitir() {
        return EstadoEmitido.INSTANCIA;
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
