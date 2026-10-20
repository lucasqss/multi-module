package com.example.processo.app.adapter.inbound.rest.dto;

import java.math.BigDecimal;

/** Corpo do POST /processos/veiculares. */
public record NovoProcessoVeicularRequest(ProponenteRequest proponente, BigDecimal valor, VeiculoRequest veiculo) {
}
