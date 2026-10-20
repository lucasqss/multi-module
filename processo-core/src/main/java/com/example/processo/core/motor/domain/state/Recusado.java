package com.example.processo.core.motor.domain.state;

/** Final: recusado no processamento, com motivo. Não aceita nenhum evento. */
final class Recusado implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.RECUSADO;
    }
}
