package com.example.processo.app.adapter.inbound.rest.dto;

/** CPF só com os 11 dígitos. */
public record ProponenteRequest(String nome, String cpf) {
}
