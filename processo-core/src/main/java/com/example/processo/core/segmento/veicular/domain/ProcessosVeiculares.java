package com.example.processo.core.segmento.veicular.domain;

import com.example.processo.core.motor.domain.Processo;

import java.util.Optional;
import java.util.UUID;

public interface ProcessosVeiculares {

    /** Grava o pai (PROCESSO) e a filha (PROCESSO_VEICULAR). */
    void salvar(Processo<DadosVeiculares> processo);

    Optional<Processo<DadosVeiculares>> porId(UUID id);
}
