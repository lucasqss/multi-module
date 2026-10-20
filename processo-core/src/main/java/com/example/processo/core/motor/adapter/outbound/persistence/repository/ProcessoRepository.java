package com.example.processo.core.motor.adapter.outbound.persistence.repository;

import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

import java.util.Optional;
import java.util.UUID;

/** Jakarta Data puro, sobre a tabela pai: o que o motor precisa sem conhecer o segmento. */
@Repository
public interface ProcessoRepository {

    @Query("select segmento from ProcessoEntity where id = :id")
    Optional<String> buscarSegmento(UUID id);

    /**
     * SQL nativo de propósito: a situação está só na tabela pai, e um update HQL numa hierarquia JOINED faz o
     * Hibernate usar a estratégia multi-tabela (tabelas temporárias no Oracle).
     */
    @SQL("update PROCESSO set SITUACAO = :para, MOTIVO_RECUSA = :motivo where ID = :id and SITUACAO = :de")
    int atualizarSituacao(UUID id, String de, String para, String motivo);
}
