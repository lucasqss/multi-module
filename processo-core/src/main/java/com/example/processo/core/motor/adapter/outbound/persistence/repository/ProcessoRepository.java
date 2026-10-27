package com.example.processo.core.motor.adapter.outbound.persistence.repository;

import com.example.jpacore.ChaveIdempotenciaEntity;
import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.domain.state.Situacao;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

import java.util.Optional;
import java.util.UUID;

/** Implementa o port do motor sobre a tabela pai: o que o motor precisa sem conhecer o segmento. */
@Repository
public interface ProcessoRepository extends Processos {

    @Query("select segmento from ProcessoEntity where id = :id")
    Optional<String> buscarSegmento(UUID id);

    /**
     * SQL nativo de propósito: a situação está só na tabela pai, e um update HQL numa hierarquia JOINED faz o
     * Hibernate usar a estratégia multi-tabela (tabelas temporárias no Oracle).
     */
    @SQL("update PROCESSO set SITUACAO = :para, MOTIVO_RECUSA = :motivo where ID = :id and SITUACAO = :de")
    int atualizarSituacao(UUID id, String de, String para, String motivo);

    @Find
    Optional<ChaveIdempotenciaEntity> buscarChave(String chave);

    @Insert
    void inserirChave(ChaveIdempotenciaEntity chave);

    @Override
    default Optional<UUID> porChave(String chave) {
        return buscarChave(chave).map(ChaveIdempotenciaEntity::getProcessoId);
    }

    @Override
    default void registrarChave(String chave, UUID processoId) {
        inserirChave(new ChaveIdempotenciaEntity(chave, processoId));
    }

    @Override
    default Optional<String> segmentoDe(UUID id) {
        return buscarSegmento(id);
    }

    /** Compare-and-set: só uma linha muda se a situação ainda for a anterior. */
    @Override
    default boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
        return atualizarSituacao(id, de.name(), para.name(), motivoRecusa) == 1;
    }
}
