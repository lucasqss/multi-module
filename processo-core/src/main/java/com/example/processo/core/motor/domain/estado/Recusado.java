package com.example.processo.core.motor.domain.estado;

/** Final: recusado no processamento, com motivo. Não aceita nenhum evento. */
final class Recusado implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.RECUSADO;
    }
}
