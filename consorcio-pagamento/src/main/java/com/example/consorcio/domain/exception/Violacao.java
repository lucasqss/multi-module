package com.example.consorcio.domain.exception;

/** Uma regra de negócio não atendida: código estável (para máquinas) + mensagem (para pessoas). */
public record Violacao(String codigo, String mensagem) {
}
