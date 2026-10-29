package br.com.consorcio.utilizacao.dominio.nucleo.processo.api;

import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi.GarantiaResumo;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi.CotaReservada;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.Papel;

import java.math.BigDecimal;
import java.util.List;

/** Ciclo de vida do processo: estado macro, etapas e consultas. Língua do negócio, sem formato de tela. */
public interface ProcessoApi {

    record EtapaResumo(Fase fase, SituacaoEtapa situacao) {
    }

    record ObjetoResumo(Long id, String descricao, BigDecimal valorNegociado, BigDecimal valorAvaliado, BigDecimal valorBase) {
    }

    record ProcessoResumo(Long id, TipoProcesso tipo, Categoria categoria, Finalidade finalidade, EstadoProcesso estado,
                          String roteiro, Long titularPessoaId, List<ObjetoResumo> objetos, List<EtapaResumo> etapas) {

        public long fasesResolvidas() {
            return etapas.stream().filter(e -> e.situacao().resolvida()).count();
        }
    }

    /** Visão composta usada igual por backoffice e consorciado; cada canal dá a forma de tela. */
    record LinhaDoTempo(ProcessoResumo processo, List<CotaReservada> cotas, List<GarantiaResumo> garantias) {
    }

    ProcessoResumo resumo(Long processoId);

    LinhaDoTempo linhaDoTempo(Long processoId);

    List<LinhaDoTempo> doTitular(String cpf);

    Long adicionarParticipante(Long processoId, String tipoPessoa, String cpfCnpj, String nome, Papel papel);

    CotaReservada reservarCota(Long processoId, String cotaRef, BigDecimal valor, String ator);

    void confirmarComunicacaoFormal(Long processoId, String ator);

    void informarAvaliacao(Long processoId, Long objetoId, BigDecimal valorAvaliado, String ator);

    /** Conclusão por um usuário. Fases automáticas são recusadas. */
    void concluirEtapa(Long processoId, Fase fase, String ator);

    /** Conclusão pelo sistema (ex.: liquidação conclui LIBERACAO_PAGAMENTO). */
    void concluirPeloSistema(Long processoId, Fase fase, String ator);

    void mudarEstado(Long processoId, EstadoProcesso novo, String motivo, String ator);

    void cancelar(Long processoId, String motivo, String ator);

    /** Pertença: o objeto e/ou a participação informados são deste processo. Nulos são ignorados. */
    void exigirPertenca(Long processoId, Long objetoId, Long participacaoId);
}
