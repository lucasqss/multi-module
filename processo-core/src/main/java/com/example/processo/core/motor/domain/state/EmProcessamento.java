package com.example.processo.core.motor.domain.state;

/** O segmento está avaliando: termina concluído ou recusado. Não pode mais ser cancelado. */
final class EmProcessamento implements EstadoDoProcesso {

    @Override
    public Situacao situacao() {
        return Situacao.EM_PROCESSAMENTO;
    }

    @Override
    public EstadoDoProcesso concluir() {
        return CONCLUIDO;
    }

    @Override
    public EstadoDoProcesso recusar() {
        return RECUSADO;
    }
}
