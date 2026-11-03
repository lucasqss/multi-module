package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.PagadorInvalidoException;

import java.util.Objects;

/** Quem paga o boleto. O e-mail é o destinatário principal da notificação de emissão. */
public record Pagador(String nome, Documento documento, Email email) {

    public Pagador {
        if (nome == null || nome.isBlank()) {
            throw new PagadorInvalidoException("nome é obrigatório");
        }
        Objects.requireNonNull(documento, "documento é obrigatório");
        Objects.requireNonNull(email, "e-mail é obrigatório");
        nome = nome.strip();
    }

    public static Pagador de(String nome, String documento, String email) {
        return new Pagador(nome, new Documento(documento), new Email(email));
    }
}
