package com.example.processo.core.segmento.veicular.adapter.outbound.persistence.repository;

import com.example.jpacore.ProcessoVeicularEntity;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.veicular.adapter.outbound.persistence.mapper.ProcessoVeicularMapper;
import com.example.processo.core.segmento.veicular.application.port.out.ProcessosVeiculares;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementa o port do segmento: os métodos Jakarta Data trabalham com a entidade e os {@code default} convertem
 * pelo mapper.
 */
@Repository
public interface ProcessoVeicularRepository extends ProcessosVeiculares {

    @Insert
    void inserir(ProcessoVeicularEntity processo);

    @Find
    Optional<ProcessoVeicularEntity> buscar(UUID id);

    @Override
    default void salvar(Processo<DadosVeiculares> processo) {
        inserir(ProcessoVeicularMapper.paraEntidade(processo));
    }

    @Override
    default Optional<Processo<DadosVeiculares>> porId(UUID id) {
        return buscar(id).map(ProcessoVeicularMapper::paraDominio);
    }
}
