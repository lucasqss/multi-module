package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.Destinatarios;
import com.example.boleto.domain.valueobject.Email;

import java.util.List;
import java.util.UUID;

/** A seção de destinatários inteira (semântica de PUT: substitui o que havia). */
public record SalvarDestinatariosBoletoInput(UUID id, List<String> emails) {

    public Destinatarios destinatarios() {
        return new Destinatarios(emails.stream().map(Email::new).toList());
    }
}
