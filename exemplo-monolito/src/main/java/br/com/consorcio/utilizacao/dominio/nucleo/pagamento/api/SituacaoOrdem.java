package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api;

/** Ordem rejeitada ou devolvida nunca reabre: cria-se outra. */
public enum SituacaoOrdem {
    PREPARADA, APROVADA, ENVIADA, LIQUIDADA, REJEITADA, DEVOLVIDA, CANCELADA;

    public boolean encerrada() {
        return this == LIQUIDADA || this == REJEITADA || this == DEVOLVIDA || this == CANCELADA;
    }
}
