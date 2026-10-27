package com.example.processo.core.motor.application.port.spi;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;

import java.util.Optional;
import java.util.UUID;

/**
 * O mínimo que o motor precisa da persistência de um segmento: gravar e ler o processo inteiro (pai + filha). O port
 * de cada segmento estende esta interface e acrescenta as consultas próprias dele.
 */
public interface PersistenciaDoSegmento<D extends DadosDoSegmento> {

    void salvar(Processo<D> processo);

    Optional<Processo<D>> porId(UUID id);
}
