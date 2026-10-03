package com.example.biblioteca.command.domain;

import java.util.UUID;

/** Port: o domínio declara o que precisa; a persistência (adapter) decide como. */
public interface Livros {

    void salvar(Livro livro);

    boolean existeIsbn(String isbn);

    boolean existe(UUID id);
}
