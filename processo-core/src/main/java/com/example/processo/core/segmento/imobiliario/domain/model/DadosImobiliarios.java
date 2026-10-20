package com.example.processo.core.segmento.imobiliario.domain.model;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;

import java.math.BigDecimal;

/** O que um processo imobiliário tem além dos dados comuns: o imóvel. */
public record DadosImobiliarios(String matricula, TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf)
        implements DadosDoSegmento {

    public enum TipoImovel {
        RESIDENCIAL(new BigDecimal("0.80")),
        COMERCIAL(new BigDecimal("0.60"));

        /** Fração máxima da avaliação que o valor do processo pode alcançar. */
        public final BigDecimal limiteDaAvaliacao;

        TipoImovel(BigDecimal limiteDaAvaliacao) {
            this.limiteDaAvaliacao = limiteDaAvaliacao;
        }
    }
}
