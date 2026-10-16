package com.example.stringgenerator;

import java.util.stream.Collectors;

/** Referência de livro (NBR 6023): AUTORES. Título: subtítulo. 2. ed. Local: Editora, ano. */
public final class ReferenciaAbnt {

    /** A partir deste número de autores, cita-se só o primeiro seguido de "et al.". */
    static final int LIMITE_AUTORES = 4;

    private ReferenciaAbnt() {
    }

    public static String de(ObraRef obra) {
        var ref = new StringBuilder();
        ref.append(terminarComPonto(autores(obra))).append(' ');
        ref.append(obra.titulo());
        if (preenchido(obra.subtitulo())) {
            ref.append(": ").append(obra.subtitulo());
        }
        ref.append(". ");
        if (obra.edicao() > 1) {
            ref.append(obra.edicao()).append(". ed. ");
        }
        ref.append(preenchido(obra.local()) ? obra.local() : "[S. l.]").append(": ");
        ref.append(preenchido(obra.editora()) ? obra.editora() : "[s. n.]").append(", ");
        return ref.append(obra.ano()).append('.').toString();
    }

    private static String autores(ObraRef obra) {
        if (obra.autores().size() >= LIMITE_AUTORES) {
            return NomeAbnt.inverter(obra.autores().getFirst().nomeCompleto()) + " et al.";
        }
        return obra.autores().stream()
                .map(a -> NomeAbnt.inverter(a.nomeCompleto()))
                .collect(Collectors.joining("; "));
    }

    /** Evita ".." quando a entrada já termina em ponto (ex.: "et al."). */
    private static String terminarComPonto(String texto) {
        return texto.endsWith(".") ? texto : texto + ".";
    }

    static boolean preenchido(String texto) {
        return texto != null && !texto.isBlank();
    }
}
