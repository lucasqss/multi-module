package com.example.biblioteca.command.domain;

import java.util.List;
import java.util.UUID;

/** Obra do acervo. Os exemplares físicos são um agregado à parte, referenciados por id. */
public record Livro(UUID id, String isbn, String titulo, String subtitulo, List<String> autores,
                    int edicao, String local, String editora, int ano) {

    public Livro {
        exigir(isbn != null && isbn.matches("\\d{13}"), "ISBN deve ter 13 dígitos");
        exigir(titulo != null && !titulo.isBlank(), "Título é obrigatório");
        exigir(autores != null && !autores.isEmpty(), "Informe ao menos um autor");
        exigir(edicao >= 1, "Edição deve ser maior ou igual a 1");
        autores = List.copyOf(autores);
    }

    public static Livro cadastrar(String isbn, String titulo, String subtitulo, List<String> autores,
                                  int edicao, String local, String editora, int ano) {
        return new Livro(UUID.randomUUID(), isbn, titulo, subtitulo, autores, edicao, local, editora, ano);
    }

    private static void exigir(boolean condicao, String motivo) {
        if (!condicao) {
            throw new RegraVioladaException(motivo);
        }
    }
}
