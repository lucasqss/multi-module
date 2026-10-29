package br.com.consorcio.utilizacao.dominio.nucleo.processo.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi.CotaReservada;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.Papel;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.PartesApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.*;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo.*;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.AberturaDeProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.FaseRoteiro;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.PreCondicaoFase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.plugin.Roteiro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/**
 * Casos de uso do processo. A jornada de negócio (que fase vem depois, qual portão move o estado)
 * vem do roteiro do plug-in; as regras de "deve / não pode" ficam nas entidades e nas pré-condições.
 */
@ApplicationScoped
public class ProcessoAplicacao implements ProcessoApi, AberturaDeProcesso {

    @Inject
    ProcessoRepository processos;

    @Inject
    HistoricoRepository historico;

    @Inject
    Modalidades modalidades;

    @Inject
    Instance<PreCondicaoFase> regrasDoNucleo;

    @Inject
    PartesApi partes;

    @Inject
    CreditoApi credito;

    @Inject
    GarantiaApi garantias;

    // ------------------------------------------------------------------ comandos

    @Override
    @Transactional
    public Long abrir(Processo novo, Objeto objeto, String tipoPessoa, String cpfTitular, String nomeTitular, String ator) {
        Roteiro roteiro = modalidades.vigente(novo.tipo(), novo.finalidade());
        PartesApi.PessoaResumo titular = partes.obterOuCriarPessoa(tipoPessoa, cpfTitular, nomeTitular);
        novo.iniciar(roteiro, titular.id(), objeto);
        processos.persist(novo);
        partes.vincular(novo.id(), titular.id(), Papel.COMPRADOR);
        registrar(novo.id(), "PROCESSO", novo.id(), null, novo.estado(), "aberto pelo roteiro " + roteiro.codigo(), ator);
        return novo.id();
    }

    @Override
    @Transactional
    public Long adicionarParticipante(Long processoId, String tipoPessoa, String cpfCnpj, String nome, Papel papel) {
        buscar(processoId).exigirNaoTerminal();
        PartesApi.PessoaResumo pessoa = partes.obterOuCriarPessoa(tipoPessoa, cpfCnpj, nome);
        return partes.vincular(processoId, pessoa.id(), papel);
    }

    /** Reserva a cota e, na primeira reserva, conclui a fase "crédito disponível". */
    @Override
    @Transactional
    public CotaReservada reservarCota(Long processoId, String cotaRef, BigDecimal valor, String ator) {
        Processo p = buscar(processoId);
        p.exigirNaoTerminal();
        String cpfTitular = partes.pessoa(p.titularPessoaId()).cpfCnpj();
        CotaReservada cota = credito.reservar(p.id(), cotaRef, valor, p.categoria().name(), cpfTitular);
        registrar(p.id(), "COTA", cota.id(), null, cota.situacao(), cotaRef + " " + valor, ator);
        if (p.etapaPendente(Fase.CREDITO_DISPONIVEL)) {
            concluir(p, Fase.CREDITO_DISPONIVEL, ator);
        }
        return cota;
    }

    @Override
    @Transactional
    public void confirmarComunicacaoFormal(Long processoId, String ator) {
        Processo p = buscar(processoId);
        p.registrarComunicacaoFormal();
        concluir(p, Fase.ABERTURA, ator);
    }

    @Override
    @Transactional
    public void informarAvaliacao(Long processoId, Long objetoId, BigDecimal valorAvaliado, String ator) {
        Processo p = buscar(processoId);
        p.objeto(objetoId).informarAvaliacao(valorAvaliado);
        registrar(p.id(), "OBJETO", objetoId, null, valorAvaliado, "valor avaliado", ator);
    }

    @Override
    @Transactional
    public void concluirEtapa(Long processoId, Fase fase, String ator) {
        Processo p = buscar(processoId);
        RegraNegocioException.exigir(!faseDoRoteiro(p, fase).automatica(), "FASE_AUTOMATICA", fase + " é concluída pelo sistema");
        concluir(p, fase, ator);
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void concluirPeloSistema(Long processoId, Fase fase, String ator) {
        concluir(buscar(processoId), fase, ator);
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void mudarEstado(Long processoId, EstadoProcesso novo, String motivo, String ator) {
        Processo p = buscar(processoId);
        EstadoProcesso anterior = p.mudarEstado(novo);
        registrar(p.id(), "PROCESSO", p.id(), anterior, novo, motivo, ator);
    }

    /** Libera as cotas e cancela garantias ainda não constituídas. */
    @Override
    @Transactional
    public void cancelar(Long processoId, String motivo, String ator) {
        mudarEstado(processoId, EstadoProcesso.CANCELADO, motivo, ator);
        credito.encerrarReservas(processoId, CreditoApi.Encerramento.LIBERADAS);
        garantias.cancelarPropostasDoProcesso(processoId, ator);
    }

    @Override
    public void exigirPertenca(Long processoId, Long objetoId, Long participacaoId) {
        Processo p = buscar(processoId);
        if (objetoId != null) {
            p.objeto(objetoId);
        }
        if (participacaoId != null) {
            RegraNegocioException.exigir(partes.participacao(participacaoId).processoId().equals(processoId),
                    "PARTICIPACAO_DE_OUTRO_PROCESSO", "Participação não pertence ao processo " + processoId);
        }
    }

    // ------------------------------------------------------------------ consultas

    @Override
    public ProcessoResumo resumo(Long processoId) {
        return resumo(buscar(processoId));
    }

    @Override
    public LinhaDoTempo linhaDoTempo(Long processoId) {
        return new LinhaDoTempo(resumo(processoId), credito.doProcesso(processoId), garantias.doProcesso(processoId));
    }

    @Override
    public List<LinhaDoTempo> doTitular(String cpf) {
        return partes.pessoaPorDocumento(cpf)
                .map(pessoa -> processos.list("titularPessoaId", pessoa.id()).stream().map(p -> linhaDoTempo(p.id())).toList())
                .orElse(List.of());
    }

    // ------------------------------------------------------------------ apoio

    private void concluir(Processo p, Fase fase, String ator) {
        FaseRoteiro fr = faseDoRoteiro(p, fase);
        Stream.concat(regrasDoNucleo.stream(), modalidades.de(p.tipo()).regras().stream())
                .filter(r -> r.aplicaA(fr))
                .forEach(r -> r.verificar(p));
        Processo.Conclusao c = p.concluir(fr, ator);
        registrar(p.id(), "ETAPA", c.etapa().id(), null, SituacaoEtapa.CONCLUIDA, fase.name(), ator);
        c.para().ifPresent(novo -> registrar(p.id(), "PROCESSO", p.id(), c.de().orElse(null), novo, "portão " + fase, ator));
    }

    private FaseRoteiro faseDoRoteiro(Processo p, Fase fase) {
        return modalidades.roteiroDe(p).fase(fase).orElseThrow(() ->
                new RegraNegocioException("FASE_FORA_DO_ROTEIRO", fase + " não faz parte do roteiro " + p.roteiroCodigo()));
    }

    private void registrar(Long processoId, String entidade, Long entidadeId, Object de, Object para, String motivo, String ator) {
        historico.persist(new Historico(processoId, entidade, entidadeId, de, para, motivo, ator));
    }

    private Processo buscar(Long id) {
        return processos.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Processo", id));
    }

    private static ProcessoResumo resumo(Processo p) {
        return new ProcessoResumo(p.id(), p.tipo(), p.categoria(), p.finalidade(), p.estado(),
                p.roteiroCodigo() + " v" + p.roteiroVersao(), p.titularPessoaId(),
                p.objetos().stream().map(o -> new ObjetoResumo(o.id(), o.descricao(), o.valorNegociado(), o.valorAvaliado(), o.valorBase())).toList(),
                p.etapas().stream().map(e -> new EtapaResumo(e.fase(), e.situacao())).toList());
    }
}
