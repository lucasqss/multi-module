package com.example.processo.core.segmento.imobiliario.domain;

import com.example.processo.core.motor.domain.Processo;

public interface ProcessosImobiliarios {

    boolean existeAbertoParaMatricula(String matricula, String uf);

    /** Grava o pai (PROCESSO) e a filha (PROCESSO_IMOBILIARIO). */
    void salvar(Processo<DadosImobiliarios> processo);
}
