package com.example.processo.core.segmento.veicular.adapter.outbound.persistence.repository;

import com.example.jpacore.ProcessoVeicularEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/** Jakarta Data puro, sobre a entidade filha: não conhece o domínio (quem converte é o mapper). */
@Repository
public interface ProcessoVeicularRepository {

    @Insert
    void inserir(ProcessoVeicularEntity processo);

    @Find
    Optional<ProcessoVeicularEntity> buscar(UUID id);
}
