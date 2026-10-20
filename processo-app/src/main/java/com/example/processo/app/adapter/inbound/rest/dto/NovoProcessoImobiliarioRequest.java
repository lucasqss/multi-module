package com.example.processo.app.adapter.inbound.rest.dto;

import java.math.BigDecimal;

/** Corpo do POST /processos/imobiliarios. */
public record NovoProcessoImobiliarioRequest(ProponenteRequest proponente, BigDecimal valor, ImovelRequest imovel) {
}
