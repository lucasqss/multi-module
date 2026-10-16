package com.example.biblioteca.command.domain;

import java.util.Optional;
import java.util.UUID;

public interface Leitores {

    Optional<Leitor> porId(UUID id);

    boolean existeEmail(String email);

    /** Insere ou atualiza. */
    void salvar(Leitor leitor);
}
