package com.example.processo.core.motor.adapter.persistence;

import com.example.processo.core.motor.domain.Processos;
import com.example.processo.core.motor.domain.estado.Situacao;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessosJpa extends Processos {

    @Query("select segmento from ProcessoEntity where id = :id")
    Optional<String> buscarSegmento(UUID id);

    /**
     * SQL nativo de propósito: a situação está só na tabela pai, e um update HQL numa hierarquia JOINED faz o
     * Hibernate usar a estratégia multi-tabela (tabelas temporárias no Oracle).
     */
    @SQL("update PROCESSO set SITUACAO = :para, MOTIVO_RECUSA = :motivo where ID = :id and SITUACAO = :de")
    int atualizarSituacao(UUID id, String de, String para, String motivo);

    @Override
    default Optional<String> segmentoDe(UUID id) {
        return buscarSegmento(id);
    }

    @Override
    default boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
        return atualizarSituacao(id, de.name(), para.name(), motivoRecusa) == 1;
    }
}
