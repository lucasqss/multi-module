package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api;

import java.math.BigDecimal;
import java.util.List;

public interface PagamentoApi {

    /** De onde vem cada parte do valor: um vínculo de cota ou um recurso. */
    record Origem(Long processoCotaId, Long recursoId, BigDecimal valor) {
    }

    record PrepararOrdem(Long processoId, Long beneficiarioParticipacaoId, Long contaRecebimentoId,
                         FinalidadeOrdem finalidade, String meio, List<Origem> origens) {
    }

    record OrdemResumo(Long id, Long processoId, SituacaoOrdem situacao, BigDecimal valor, String idExterno) {
    }

    OrdemResumo preparar(PrepararOrdem cmd, String ator);

    /** Quatro olhos: quem preparou não aprova. A aprovação deixa o envio na saída assíncrona. */
    OrdemResumo aprovar(Long ordemId, String ator);

    /** Retorno do sistema de pagamentos. Idempotente pela chave. */
    OrdemResumo receberRetorno(String chaveIdempotencia, Long ordemId, String resultado, String ator);

    OrdemResumo ordem(Long ordemId);
}
