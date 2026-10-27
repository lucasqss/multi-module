package com.example.processo.core.motor.application.port.out;

import com.example.processo.core.motor.domain.state.Situacao;

import java.util.Optional;
import java.util.UUID;

/**
 * O que o motor precisa do processo sem conhecer o segmento: o tipo dele, a troca de situação e a chave de
 * idempotência de quem pediu a criação.
 */
public interface Processos {

    /** O processo já criado com esta chave de idempotência, se houver. */
    Optional<UUID> porChave(String chave);

    /** Liga a chave ao processo; falha se a chave já existir (duas criações simultâneas com a mesma chave). */
    void registrarChave(String chave, UUID processoId);

    /** Código do segmento (discriminador) do processo, se ele existir. */
    Optional<String> segmentoDe(UUID id);

    /**
     * Troca a situação só se ela ainda for {@code de} (compare-and-set). {@code false} quer dizer que outra
     * requisição mudou o processo antes.
     */
    boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa);
}
