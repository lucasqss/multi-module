package com.example.processo.core.segmento.veicular.domain;

import com.example.processo.core.motor.domain.DadosDoSegmento;

import java.math.BigDecimal;

/** O que um processo veicular tem além dos dados comuns: o veículo. */
public record DadosVeiculares(String placa, Integer anoFabricacao, BigDecimal valorFipe) implements DadosDoSegmento {
}
