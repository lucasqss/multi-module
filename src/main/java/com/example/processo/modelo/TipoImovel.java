package com.example.processo.modelo;

import java.math.BigDecimal;

/** Valores da coluna PROCESSO_IMOBILIARIO.TIPO_IMOVEL; constante nova pede também o CHECK numa migration nova. */
public enum TipoImovel {
    RESIDENCIAL(new BigDecimal("0.80")),
    COMERCIAL(new BigDecimal("0.60"));

    /** Fração máxima da avaliação que o valor do processo pode alcançar. */
    public final BigDecimal limiteDaAvaliacao;

    TipoImovel(BigDecimal limiteDaAvaliacao) {
        this.limiteDaAvaliacao = limiteDaAvaliacao;
    }
}
