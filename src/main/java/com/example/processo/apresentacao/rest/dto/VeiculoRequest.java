package com.example.processo.apresentacao.rest.dto;

import java.math.BigDecimal;

public record VeiculoRequest(String placa, Integer anoFabricacao, BigDecimal valorFipe) {
}
