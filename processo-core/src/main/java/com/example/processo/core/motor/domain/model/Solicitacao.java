package com.example.processo.core.motor.domain.model;

/** Pedido de abertura de processo, antes das regras: ainda sem id, número nem data. */
public record Solicitacao<D extends DadosDoSegmento>(Proponente proponente, Valor valor, D dados) {
}
