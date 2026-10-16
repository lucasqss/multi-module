package com.example.biblioteca.command.domain;

import java.util.Optional;
import java.util.UUID;

public interface Exemplares {

    Optional<Exemplar> porId(UUID id);

    boolean existeTombo(String tombo);

    /** Insere ou atualiza. */
    void salvar(Exemplar exemplar);
}
