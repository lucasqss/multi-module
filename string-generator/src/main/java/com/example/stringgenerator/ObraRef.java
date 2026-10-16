package com.example.stringgenerator;

import java.util.List;

/** Dados mínimos de uma obra para gerar referência e citação. {@code local}, {@code editora} e {@code subtitulo} são opcionais. */
public record ObraRef(List<AutorRef> autores, String titulo, String subtitulo, int edicao,
                      String local, String editora, int ano) {

    public ObraRef {
        if (autores == null || autores.isEmpty()) {
            throw new IllegalArgumentException("A obra precisa de ao menos um autor");
        }
        autores = List.copyOf(autores);
    }
}
