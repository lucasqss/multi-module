package com.example.processo.core.segmento.imobiliario.application.port.out;

import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

import java.util.Optional;
import java.util.UUID;

public interface ProcessosImobiliarios {

    /** Em andamento = aberto ou em processamento. */
    boolean existeEmAndamentoParaMatricula(String matricula, String uf);

    /** Grava o pai (PROCESSO) e a filha (PROCESSO_IMOBILIARIO). */
    void salvar(Processo<DadosImobiliarios> processo);

    Optional<Processo<DadosImobiliarios>> porId(UUID id);
}
