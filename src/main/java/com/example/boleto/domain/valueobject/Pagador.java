package com.example.boleto.domain.valueobject;

import com.example.boleto.domain.exception.PagadorInvalidoException;

import java.util.Objects;

/** Quem paga o boleto. O e-mail é para onde vai a notificação de emissão. */
public record Pagador(String nome, Documento documento, String email) {

    public Pagador {
        if (nome == null || nome.isBlank()) {
            throw new PagadorInvalidoException("nome é obrigatório");
        }
        Objects.requireNonNull(documento, "documento é obrigatório");
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new PagadorInvalidoException("e-mail inválido");
        }
        nome = nome.strip();
    }

    public static Pagador de(String nome, String documento, String email) {
        return new Pagador(nome, new Documento(documento), email);
    }
}
