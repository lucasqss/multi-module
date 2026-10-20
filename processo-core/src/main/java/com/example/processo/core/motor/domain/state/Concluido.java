package com.example.processo.core.motor.domain.state;

/** Final: aprovado no processamento. Não aceita nenhum evento. */
final class Concluido implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.CONCLUIDO;
    }
}
