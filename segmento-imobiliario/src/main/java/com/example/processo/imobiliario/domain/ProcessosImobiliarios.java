package com.example.processo.imobiliario.domain;

import com.example.processo.motor.domain.Processo;

public interface ProcessosImobiliarios {

    boolean existeAbertoParaMatricula(String matricula, String uf);

    /** Grava o pai (PROCESSO) e a filha (PROCESSO_IMOBILIARIO). */
    void salvar(Processo<DadosImobiliarios> processo);
}
