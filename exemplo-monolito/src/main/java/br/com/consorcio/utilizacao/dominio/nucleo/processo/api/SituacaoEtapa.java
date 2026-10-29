package br.com.consorcio.utilizacao.dominio.nucleo.processo.api;

public enum SituacaoEtapa {
    NAO_INICIADA, EM_ANDAMENTO, CONCLUIDA, DISPENSADA, BLOQUEADA;

    public boolean resolvida() {
        return this == CONCLUIDA || this == DISPENSADA;
    }
}
