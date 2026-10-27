package com.example.processo.core.motor.domain.model;

/** Quem pede o processo. O CPF já chega válido ({@link Cpf}); o nome é conferido pelas regras comuns. */
public record Proponente(String nome, Cpf cpf) {

    /** Atalho para a borda e os testes: o CPF em texto é convertido (e validado) aqui. */
    public Proponente(String nome, String cpf) {
        this(nome, new Cpf(cpf));
    }
}
