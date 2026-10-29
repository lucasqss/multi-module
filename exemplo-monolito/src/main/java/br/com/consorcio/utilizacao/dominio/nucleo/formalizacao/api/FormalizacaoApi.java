package br.com.consorcio.utilizacao.dominio.nucleo.formalizacao.api;

import java.util.Optional;

public interface FormalizacaoApi {

    record InstrumentoResumo(Long id, Long processoId, String tipo, String situacao) {
    }

    /** F9: instrumento de compra e venda com AF; propõe uma alienação por cota reservada. */
    InstrumentoResumo redigirCompraVendaComAlienacao(Long processoId, String ator);

    Optional<InstrumentoResumo> instrumentoDo(Long processoId);
}
