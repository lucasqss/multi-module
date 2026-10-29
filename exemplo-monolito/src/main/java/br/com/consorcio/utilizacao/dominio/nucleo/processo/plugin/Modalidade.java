package br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin;

import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.TipoProcesso;

import java.util.List;

/**
 * Contrato de plug-in do microkernel. Cada modalidade (imóvel, veículo, serviço...) implementa esta
 * interface num pacote próprio de dominio.modalidade; o núcleo nunca conhece as implementações.
 */
public interface Modalidade {

    TipoProcesso tipo();

    /** Roteiros por finalidade, possivelmente várias versões. */
    List<Roteiro> roteiros();

    /** Regras que só valem para esta modalidade. */
    default List<PreCondicaoFase> regras() {
        return List.of();
    }
}
