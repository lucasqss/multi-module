package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.TipoImovel;

import java.math.BigDecimal;

public record ImovelRequest(String matricula, TipoImovel tipo, BigDecimal valorAvaliacao, String uf) {
}
