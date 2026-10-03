package com.example.biblioteca.command.domain;

import java.time.LocalDate;
import java.util.UUID;

/** @param suspensoAte último dia (inclusive) em que o leitor não pode emprestar; {@code null} se nunca foi suspenso */
public record Leitor(UUID id, String nome, String email, LocalDate suspensoAte) {

    public static Leitor cadastrar(String nome, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("Nome é obrigatório");
        }
        if (email == null || !email.contains("@")) {
            throw new RegraVioladaException("E-mail inválido");
        }
        return new Leitor(UUID.randomUUID(), nome, email, null);
    }

    public boolean suspensoEm(LocalDate dia) {
        return suspensoAte != null && !dia.isAfter(suspensoAte);
    }

    /** Um dia de suspensão por dia de atraso, contados a partir de hoje; nunca encurta uma suspensão vigente. */
    public Leitor suspenderPorAtraso(long diasDeAtraso, LocalDate hoje) {
        var ate = hoje.plusDays(diasDeAtraso);
        if (suspensoAte != null && suspensoAte.isAfter(ate)) {
            return this;
        }
        return new Leitor(id, nome, email, ate);
    }
}
