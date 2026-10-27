package com.example.processo.app.adapter.inbound.rest.dto;

/**
 * Corpo de toda resposta de erro, no formato Problem Details (RFC 9457), servido como
 * {@code application/problem+json}. {@code type} identifica o tipo de problema (estável, para o cliente decidir);
 * {@code title} o resume; {@code detail} é o motivo desta ocorrência, apresentável ao usuário.
 */
public record Problema(String type, String title, int status, String detail) {

    public static final String MEDIA_TYPE = "application/problem+json";
}
