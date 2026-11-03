package com.example.boleto.domain.valueobject;

import java.util.HashSet;
import java.util.List;

/** Seção opcional: e-mails que recebem a notificação de emissão além do pagador. */
public record Destinatarios(List<Email> emails) {

    public static final Destinatarios NENHUM = new Destinatarios(List.of());

    public Destinatarios {
        emails = List.copyOf(emails);
    }

    public int quantidade() {
        return emails.size();
    }

    public boolean possuiRepetidos() {
        return new HashSet<>(emails).size() != emails.size();
    }

    public boolean contem(Email email) {
        return emails.contains(email);
    }
}
