package com.example.processo.app.adapter.inbound.rest.dto;

import java.math.BigDecimal;

public record VeiculoRequest(String placa, Integer anoFabricacao, BigDecimal valorFipe) {
}
