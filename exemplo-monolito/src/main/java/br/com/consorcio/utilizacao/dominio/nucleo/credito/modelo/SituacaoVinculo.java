package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

public enum SituacaoVinculo {
    /** Único estado ativo: o índice ux_cre_cota_ativa impede a mesma cota RESERVADA em dois processos. */
    RESERVADA, UTILIZADA, LIBERADA
}
