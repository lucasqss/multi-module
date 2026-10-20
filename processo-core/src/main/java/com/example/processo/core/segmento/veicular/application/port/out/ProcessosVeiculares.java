package com.example.processo.core.segmento.veicular.application.port.out;

import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.veicular.domain.model.DadosVeiculares;

import java.util.Optional;
import java.util.UUID;

public interface ProcessosVeiculares {

    /** Grava o pai (PROCESSO) e a filha (PROCESSO_VEICULAR). */
    void salvar(Processo<DadosVeiculares> processo);

    Optional<Processo<DadosVeiculares>> porId(UUID id);
}
