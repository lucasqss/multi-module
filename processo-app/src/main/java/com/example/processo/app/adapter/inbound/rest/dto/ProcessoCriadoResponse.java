package com.example.processo.app.adapter.inbound.rest.dto;

import java.util.UUID;

/** Corpo do 201 de qualquer segmento: o id e o número gerado. */
public record ProcessoCriadoResponse(UUID id, String numero) {
}
