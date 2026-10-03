package com.example.stringgenerator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenciaAbntTest {

    private static ObraRef obra(List<String> autores, String subtitulo, int edicao, String local, String editora) {
        return new ObraRef(autores.stream().map(AutorRef::new).toList(),
                "Dom Casmurro", subtitulo, edicao, local, editora, 1899);
    }

    @Test
    void umAutorPrimeiraEdicaoOmiteEdicao() {
        var ref = ReferenciaAbnt.de(obra(List.of("Machado de Assis"), null, 1, "Rio de Janeiro", "Garnier"));
        assertEquals("ASSIS, Machado de. Dom Casmurro. Rio de Janeiro: Garnier, 1899.", ref);
    }

    @Test
    void subtituloEEdicaoAPartirDaSegunda() {
        var ref = ReferenciaAbnt.de(obra(List.of("Machado de Assis"), "romance", 2, "Rio de Janeiro", "Garnier"));
        assertEquals("ASSIS, Machado de. Dom Casmurro: romance. 2. ed. Rio de Janeiro: Garnier, 1899.", ref);
    }

    @Test
    void ateTresAutoresSeparadosPorPontoEVirgula() {
        var ref = ReferenciaAbnt.de(obra(List.of("Machado de Assis", "José de Alencar", "Graciliano Ramos"),
                null, 1, "Rio de Janeiro", "Garnier"));
        assertEquals("ASSIS, Machado de; ALENCAR, José de; RAMOS, Graciliano. Dom Casmurro. Rio de Janeiro: Garnier, 1899.", ref);
    }

    @Test
    void quatroOuMaisAutoresUsaEtAl() {
        var autores = Stream.of("Machado de Assis", "José de Alencar", "Graciliano Ramos", "Érico Veríssimo").toList();
        var ref = ReferenciaAbnt.de(obra(autores, null, 1, "Rio de Janeiro", "Garnier"));
        assertEquals("ASSIS, Machado de et al. Dom Casmurro. Rio de Janeiro: Garnier, 1899.", ref);
    }

    @Test
    void semLocalESemEditoraUsaAbreviaturasDaNorma() {
        var ref = ReferenciaAbnt.de(obra(List.of("Machado de Assis"), null, 1, null, " "));
        assertEquals("ASSIS, Machado de. Dom Casmurro. [S. l.]: [s. n.], 1899.", ref);
    }
}
