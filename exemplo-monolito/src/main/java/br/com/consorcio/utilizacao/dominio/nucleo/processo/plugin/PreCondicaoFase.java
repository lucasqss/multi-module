package br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.Processo;

/**
 * Regra verificada antes de concluir uma fase. De preferência, ligue a regra ao papel da fase no roteiro
 * (ex.: "o portão para APTO_PAGAMENTO"), e não ao nome: assim ela vale para todas as modalidades.
 * Regras do núcleo são beans CDI; regras de uma modalidade vêm por {@link Modalidade#regras()}.
 */
public interface PreCondicaoFase {

    boolean aplicaA(FaseRoteiro fase);

    /** Lança RegraNegocioException se não estiver satisfeita. */
    void verificar(Processo processo);
}
