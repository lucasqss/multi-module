package br.com.consorcio.utilizacao.dominio.nucleo.processo.api;

import java.util.EnumSet;
import java.util.Set;

/** Estado macro, comum a todas as modalidades. As fases em paralelo ficam nas etapas. */
public enum EstadoProcesso {
    RASCUNHO, ABERTO, EM_ANALISE, PENDENTE, APROVADO, REPROVADO, EM_FORMALIZACAO,
    EM_CONSTITUICAO_GARANTIA, APTO_PAGAMENTO, EM_PAGAMENTO, PAGO, CONCLUIDO, CANCELADO;

    public Set<EstadoProcesso> proximos() {
        return switch (this) {
            case RASCUNHO -> EnumSet.of(ABERTO, CANCELADO);
            case ABERTO -> EnumSet.of(EM_ANALISE, CANCELADO);
            case EM_ANALISE -> EnumSet.of(PENDENTE, APROVADO, REPROVADO, CANCELADO);
            case PENDENTE -> EnumSet.of(EM_ANALISE, CANCELADO);
            case APROVADO -> EnumSet.of(EM_FORMALIZACAO, CANCELADO);
            case EM_FORMALIZACAO -> EnumSet.of(EM_CONSTITUICAO_GARANTIA, APTO_PAGAMENTO, CANCELADO);
            case EM_CONSTITUICAO_GARANTIA -> EnumSet.of(APTO_PAGAMENTO);
            case APTO_PAGAMENTO -> EnumSet.of(EM_PAGAMENTO);
            case EM_PAGAMENTO -> EnumSet.of(PAGO, APTO_PAGAMENTO);
            case PAGO -> EnumSet.of(CONCLUIDO);
            case REPROVADO, CONCLUIDO, CANCELADO -> EnumSet.noneOf(EstadoProcesso.class);
        };
    }

    public boolean terminal() {
        return proximos().isEmpty();
    }
}
