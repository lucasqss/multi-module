package br.com.consorcio.utilizacao.dominio.garantia.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Única porta do módulo garantia, para os demais módulos e para o monólito de contemplação (via REST).
 * Ancorada na cota: a garantia nasce na contemplação ou na utilização e vive até a baixa.
 */
public interface GarantiaApi {

    /** Referências por id: o módulo não depende de processo, objeto nem instrumento. */
    record PropostaAlienacao(String cotaRef, String tipo, Long processoRef, Long objetoRef, Long instrumentoRef,
                             BigDecimal valorCobertura) {
    }

    record Cobertura(String cotaRef, BigDecimal exigido, BigDecimal constituido, boolean suficiente) {
    }

    record GarantiaResumo(Long id, String cotaRef, String tipo, String situacao, BigDecimal valorCobertura) {
    }

    /** Fiança declarada formalmente na contemplação nasce constituída. Idempotente pela chave. */
    Long registrarFianca(String cotaRef, String cpfCnpjFiador, String nomeFiador, BigDecimal valorCobertura,
                         String chaveIdempotencia, String ator);

    Long proporAlienacao(PropostaAlienacao proposta, String ator);

    void registrarProtocolo(Long garantiaId, String orgao, String protocolo, String ator);

    void registrarRegistro(Long garantiaId, String numeroRegistro, LocalDate registradoEm, String ator);

    /** Conferência da matrícula (AF a favor do grupo). Torna a garantia constituída. */
    void conferirRegistro(Long garantiaId, String ator);

    Cobertura cobertura(String cotaRef, BigDecimal valorExigido);

    List<GarantiaResumo> doProcesso(Long processoRef);

    void cancelarPropostasDoProcesso(Long processoRef, String ator);
}
