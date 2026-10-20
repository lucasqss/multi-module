package com.example.processo.core.motor.domain.state;

/** Recém-criado: pode ir para processamento ou ser cancelado. */
final class Aberto implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.ABERTO;
    }

    @Override
    public EstadoDoProcesso iniciarProcessamento() {
        return EM_PROCESSAMENTO;
    }

    @Override
    public EstadoDoProcesso cancelar() {
        return CANCELADO;
    }
}
