package br.com.consorcio.utilizacao.dominio.nucleo.credito.aplicacao;

import br.com.consorcio.utilizacao.compartilhado.NaoEncontradoException;
import br.com.consorcio.utilizacao.compartilhado.RegraNegocioException;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.api.CreditoApi;
import br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class CreditoAplicacao implements CreditoApi {

    @Inject
    ConsultaCore core;

    @Inject
    ProcessoCotaRepository vinculos;

    @Inject
    RecursoRepository recursos;

    @Override
    @Transactional
    public CotaReservada reservar(Long processoId, String cotaRef, BigDecimal valor, String categoria, String cpfTitular) {
        CotaCore cota = core.cota(cotaRef).orElseThrow(() -> new NaoEncontradoException("Cota no core", cotaRef));
        // Mensagem amigável no caso comum; o índice único cobre a corrida entre duas reservas simultâneas.
        vinculos.find("cotaRef = ?1 and situacao = ?2", cotaRef, SituacaoVinculo.RESERVADA).firstResultOptional()
                .ifPresent(ativo -> {
                    throw new RegraNegocioException("MOT-015", "Cota " + cotaRef + " já está em uso no processo " + ativo.processoId());
                });
        ProcessoCota v = ProcessoCota.reservar(processoId, cota, valor, categoria, cpfTitular);
        try {
            vinculos.persistAndFlush(v);
        } catch (PersistenceException e) {
            if (violou(e, "UX_CRE_COTA_ATIVA")) {
                throw new RegraNegocioException("MOT-015", "Cota " + cotaRef + " acabou de ser reservada por outro processo");
            }
            throw e;
        }
        return resumo(v);
    }

    @Override
    public List<CotaReservada> reservadas(Long processoId) {
        return vinculos.list("processoId = ?1 and situacao = ?2", processoId, SituacaoVinculo.RESERVADA).stream()
                .map(CreditoAplicacao::resumo).toList();
    }

    @Override
    public List<CotaReservada> doProcesso(Long processoId) {
        return vinculos.list("processoId", processoId).stream().map(CreditoAplicacao::resumo).toList();
    }

    @Override
    public CotaReservada vinculo(Long vinculoId) {
        return resumo(buscar(vinculoId));
    }

    @Override
    public BigDecimal totalReservado(Long processoId) {
        return reservadas(processoId).stream().map(CotaReservada::reservado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal recursosComprovados(Long processoId) {
        return recursos.list("processoId = ?1 and situacao = 'COMPROVADO'", processoId).stream()
                .map(Recurso::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void conferirSaldoNoCore(Long vinculoId, BigDecimal valor) {
        ProcessoCota v = buscar(vinculoId);
        BigDecimal saldo = core.cota(v.cotaRef()).map(CotaCore::saldoUtilizavel).orElse(BigDecimal.ZERO);
        RegraNegocioException.exigir(saldo.compareTo(valor) >= 0, "SALDO_INSUFICIENTE",
                "Saldo atual da cota " + v.cotaRef() + " é " + saldo);
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void registrarUtilizacao(Long vinculoId, BigDecimal valor) {
        buscar(vinculoId).registrarUtilizacao(valor);
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void encerrarReservas(Long processoId, Encerramento como) {
        SituacaoVinculo destino = como == Encerramento.UTILIZADAS ? SituacaoVinculo.UTILIZADA : SituacaoVinculo.LIBERADA;
        vinculos.list("processoId = ?1 and situacao = ?2", processoId, SituacaoVinculo.RESERVADA).forEach(v -> v.encerrar(destino));
    }

    private ProcessoCota buscar(Long id) {
        return vinculos.findByIdOptional(id).orElseThrow(() -> new NaoEncontradoException("Vínculo de cota", id));
    }

    private static CotaReservada resumo(ProcessoCota v) {
        return new CotaReservada(v.id(), v.processoId(), v.cotaRef(), v.situacao().name(), v.valorReservado(),
                v.valorUtilizado(), v.disponivel());
    }

    private static boolean violou(Throwable e, String constraint) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().toUpperCase().contains(constraint)) {
                return true;
            }
        }
        return false;
    }
}
