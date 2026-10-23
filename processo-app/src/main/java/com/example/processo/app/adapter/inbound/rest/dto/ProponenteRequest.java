package com.example.processo.app.adapter.inbound.rest.dto;

import com.example.processo.core.motor.domain.model.Proponente;

/** CPF só com os 11 dígitos. */
public record ProponenteRequest(String nome, String cpf) {

    /** Sem proponente no corpo, segue nulo: quem recusa é a regra comum do motor, com a mensagem dela. */
    static Proponente paraDominio(ProponenteRequest proponente) {
        return proponente == null ? null : new Proponente(proponente.nome(), proponente.cpf());
    }
}
