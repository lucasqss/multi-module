package com.example.processo.negocio.processo;

import com.example.processo.modelo.Processo;

/**
 * O que muda de uma modalidade de consórcio para outra. Cada modalidade é um bean que implementa esta interface; o
 * {@link ProcessoService} descobre todas e escolhe pela classe do processo. Modalidade nova não altera o serviço.
 */
public interface Modalidade<P extends Processo> {

    /** A entidade da modalidade (subclasse de {@link Processo}). */
    Class<P> tipo();

    /** Prefixo do número do processo. */
    String prefixo();

    /** Regras próprias da modalidade; o serviço só as aplica depois das {@link RegrasComuns}. */
    Especificacao<P> regras();

    /**
     * A decisão própria da modalidade no processamento. Roda fora de transação: pode consultar sistema externo. Os
     * estados e as transições são os mesmos para todas ({@code Situacao}); a modalidade só decide.
     */
    Avaliacao avaliar(P processo);
}
