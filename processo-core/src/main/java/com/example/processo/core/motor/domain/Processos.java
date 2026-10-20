package com.example.processo.core.motor.domain;

import com.example.processo.core.motor.domain.estado.Situacao;

import java.util.Optional;
import java.util.UUID;

/** O que o motor precisa do processo sem conhecer o segmento: o tipo dele e a troca de situação. */
public interface Processos {

    /** Código do segmento (discriminador) do processo, se ele existir. */
    Optional<String> segmentoDe(UUID id);

    /**
     * Troca a situação só se ela ainda for {@code de} (compare-and-set). {@code false} quer dizer que outra
     * requisição mudou o processo antes.
     */
    boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa);
}
