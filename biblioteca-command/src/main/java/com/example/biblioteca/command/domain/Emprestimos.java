package com.example.biblioteca.command.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Emprestimos {

    Optional<Emprestimo> porId(UUID id);

    List<Emprestimo> abertosDo(UUID leitorId);

    /** Insere ou atualiza. */
    void salvar(Emprestimo emprestimo);
}
