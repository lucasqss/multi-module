package com.example.processo.core.motor.domain.estado;

/** Final: cancelado antes de ser processado. Não aceita nenhum evento. */
final class Cancelado implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.CANCELADO;
    }
}
