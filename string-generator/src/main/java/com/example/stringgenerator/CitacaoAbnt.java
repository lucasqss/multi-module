package com.example.stringgenerator;

import java.util.stream.Collectors;

/** Citação autor-data (NBR 10520): (ASSIS, 1899), (ASSIS; ALENCAR, 1899), (ASSIS et al., 1899). */
public final class CitacaoAbnt {

    private CitacaoAbnt() {
    }

    public static String de(ObraRef obra) {
        var autores = obra.autores().size() >= ReferenciaAbnt.LIMITE_AUTORES
                ? NomeAbnt.sobrenome(obra.autores().getFirst().nomeCompleto()) + " et al."
                : obra.autores().stream()
                        .map(a -> NomeAbnt.sobrenome(a.nomeCompleto()))
                        .collect(Collectors.joining("; "));
        return "(" + autores + ", " + obra.ano() + ")";
    }
}
