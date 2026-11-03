package com.example.consorcio.adapter.database;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** Implementação da porta {@link ProcessoPagamentoRepository} usando Panache/JPA. */
@ApplicationScoped
public class ProcessoPagamentoRepositoryPanache
        implements ProcessoPagamentoRepository, PanacheRepositoryBase<ProcessoPagamentoJpaEntity, UUID> {

    @Override
    public void salvar(ProcessoPagamento processo) {
        var existente = findById(processo.getId().valor());
        var entidade = ProcessoPagamentoMapper.paraJpa(processo,
                existente != null ? existente : new ProcessoPagamentoJpaEntity());
        if (existente == null) {
            persist(entidade);
        }
    }

    @Override
    public Optional<ProcessoPagamento> buscarPorId(ProcessoPagamentoId id) {
        return findByIdOptional(id.valor()).map(ProcessoPagamentoMapper::paraDominio);
    }

    @Override
    public boolean existeProcessoEmAbertoParaCota(IdentificadorCota cota) {
        return count("grupo = ?1 and cota = ?2 and status = ?3",
                cota.grupo(), cota.cota(), StatusProcessoPagamento.ABERTO) > 0;
    }
}
