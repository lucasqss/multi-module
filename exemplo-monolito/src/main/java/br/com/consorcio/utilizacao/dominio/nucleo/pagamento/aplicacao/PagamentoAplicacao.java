package br.com.consorcio.utilizacao.dominio.nucleo.pagamento.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.compartilhado.saida.SaidaAssincrona;
import br.com.consorcio.utilizacao.dominio.garantia.api.GarantiaApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.FinalidadeOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.PagamentoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.api.SituacaoOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.*;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.Destinatario;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.EnviarOrdem;
import br.com.consorcio.utilizacao.dominio.nucleo.pagamento.modelo.MensagensPagamento.InformarUtilizacao;
import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.PartesApi;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.EstadoProcesso;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.Fase;
import br.com.consorcio.utilizacao.dominio.nucleo.processo.api.ProcessoApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PagamentoAplicacao implements PagamentoApi {

    @Inject
    OrdemRepository ordens;

    @Inject
    RetornoRepository retornos;

    @Inject
    ProcessoApi processos;

    @Inject
    PartesApi partes;

    @Inject
    CreditoApi credito;

    @Inject
    GarantiaApi garantias;

    @Inject
    SaidaAssincrona saida;

    @Override
    @Transactional
    public OrdemResumo preparar(PrepararOrdem cmd, String ator) {
        RegraNegocioException.exigir(processos.resumo(cmd.processoId()).estado() == EstadoProcesso.APTO_PAGAMENTO, "MOT-040",
                "Processo não está apto a pagamento");
        processos.exigirPertenca(cmd.processoId(), null, cmd.beneficiarioParticipacaoId());
        partes.contaVerificada(cmd.contaRecebimentoId(), cmd.beneficiarioParticipacaoId());

        OrdemPagamento ordem = OrdemPagamento.preparar(cmd.processoId(), cmd.beneficiarioParticipacaoId(),
                cmd.contaRecebimentoId(), cmd.finalidade(), cmd.meio(), ator);
        for (Origem o : cmd.origens()) {
            if (o.processoCotaId() != null) {
                CreditoApi.CotaReservada cota = credito.vinculo(o.processoCotaId());
                RegraNegocioException.exigir(cota.processoId().equals(cmd.processoId()) && "RESERVADA".equals(cota.situacao()),
                        "ORIGEM_INVALIDA", "Cota " + cota.cotaRef() + " não está reservada para este processo");
                RegraNegocioException.exigir(o.valor().compareTo(cota.disponivel()) <= 0, "ORIGEM_ACIMA_DO_RESERVADO",
                        "Cota " + cota.cotaRef() + " tem " + cota.disponivel() + " disponível");
                credito.conferirSaldoNoCore(o.processoCotaId(), o.valor());
            }
            ordem.adicionarOrigem(o.processoCotaId(), o.recursoId(), o.valor());
        }
        ordens.persist(ordem);
        return resumo(ordem);
    }

    @Override
    @Transactional
    public OrdemResumo aprovar(Long ordemId, String ator) {
        OrdemPagamento ordem = buscar(ordemId);
        ordem.aprovar(ator);
        // Defesa em profundidade: a garantia foi verificada no portão, mas pode ter mudado desde então.
        for (OrigemOrdem o : ordem.origens()) {
            if (o.processoCotaId() != null) {
                CreditoApi.CotaReservada cota = credito.vinculo(o.processoCotaId());
                RegraNegocioException.exigir(garantias.cobertura(cota.cotaRef(), cota.reservado()).suficiente(),
                        "GARANTIA_INSUFICIENTE", "Cota " + cota.cotaRef() + " sem garantia suficiente");
            }
        }
        PartesApi.ParticipacaoResumo beneficiario = partes.participacao(ordem.beneficiarioParticipacaoId());
        PartesApi.ContaResumo conta = partes.contaVerificada(ordem.contaRecebimentoId(), ordem.beneficiarioParticipacaoId());
        saida.registrar(MensagensPagamento.ENVIAR_ORDEM, ordem.id(), "ORDEM-" + ordem.id(),
                new EnviarOrdem(ordem.id(), ordem.valor(), ordem.meio(), new Destinatario(beneficiario.pessoa().cpfCnpj(),
                        beneficiario.pessoa().nome(), conta.banco(), conta.agencia(), conta.conta(), conta.chavePix())));
        if (processos.resumo(ordem.processoId()).estado() == EstadoProcesso.APTO_PAGAMENTO) {
            processos.mudarEstado(ordem.processoId(), EstadoProcesso.EM_PAGAMENTO, "ordem " + ordem.id() + " aprovada", ator);
        }
        return resumo(ordem);
    }

    @Override
    @Transactional
    public OrdemResumo receberRetorno(String chaveIdempotencia, Long ordemId, String resultado, String ator) {
        var repetido = retornos.find("chaveIdempotencia", chaveIdempotencia).firstResultOptional();
        if (repetido.isPresent()) {
            return resumo(buscar(repetido.get().ordemId()));
        }
        OrdemPagamento ordem = buscar(ordemId);
        retornos.persist(new RetornoPagamento(ordemId, chaveIdempotencia, resultado));
        ordem.registrarResultado(resultado);
        if (ordem.situacao() == SituacaoOrdem.LIQUIDADA) {
            liquidar(ordem, ator);
        } else {
            processos.mudarEstado(ordem.processoId(), EstadoProcesso.APTO_PAGAMENTO, "ordem " + ordem.id() + " " + resultado, ator);
        }
        return resumo(ordem);
    }

    /** Na mesma transação: cotas atualizadas, "informar utilização" na saída, fase de liberação concluída. */
    private void liquidar(OrdemPagamento ordem, String ator) {
        for (OrigemOrdem o : ordem.origens()) {
            if (o.processoCotaId() != null) {
                credito.registrarUtilizacao(o.processoCotaId(), o.valor());
                String cotaRef = credito.vinculo(o.processoCotaId()).cotaRef();
                saida.registrar(MensagensPagamento.INFORMAR_UTILIZACAO, o.id(), "UTILIZACAO-" + o.id(),
                        new InformarUtilizacao(cotaRef, o.valor(), "ORIGEM-" + o.id()));
            }
        }
        boolean precoQuitado = ordens.list("processoId = ?1 and finalidade = ?2", ordem.processoId(), FinalidadeOrdem.PRECO_OBJETO)
                .stream().allMatch(x -> x.situacao().encerrada());
        if (ordem.finalidade() == FinalidadeOrdem.PRECO_OBJETO && precoQuitado) {
            processos.concluirPeloSistema(ordem.processoId(), Fase.LIBERACAO_PAGAMENTO, ator);
            credito.encerrarReservas(ordem.processoId(), CreditoApi.Encerramento.UTILIZADAS);
        }
    }

    @Override
    public OrdemResumo ordem(Long ordemId) {
        return resumo(buscar(ordemId));
    }

    private OrdemPagamento buscar(Long id) {
        return ordens.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Ordem", id));
    }

    private static OrdemResumo resumo(OrdemPagamento o) {
        return new OrdemResumo(o.id(), o.processoId(), o.situacao(), o.valor(), o.idExterno());
    }
}
