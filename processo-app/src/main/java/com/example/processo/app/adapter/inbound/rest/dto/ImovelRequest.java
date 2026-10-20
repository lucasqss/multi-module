package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;

import java.math.BigDecimal;

public record ImovelRequest(String matricula, DadosImobiliarios.TipoImovel tipo, BigDecimal valorAvaliacao, String uf) {
}
