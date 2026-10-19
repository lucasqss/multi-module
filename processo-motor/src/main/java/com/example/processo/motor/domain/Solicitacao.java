package com.example.processo.motor.domain;

import java.math.BigDecimal;

/** Pedido de abertura de processo, antes das regras: ainda sem id, número nem data. */
public record Solicitacao<D extends DadosDoSegmento>(Proponente proponente, BigDecimal valor, D dados) {
}
