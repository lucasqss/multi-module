package com.example.stringgenerator;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Entrada de autor no padrão ABNT NBR 6023: SOBRENOME, Prenomes. */
public final class NomeAbnt {

    static final Locale PT_BR = Locale.of("pt", "BR");

    /** Indicam parentesco e acompanham o último sobrenome: "João Silva Filho" → "SILVA FILHO, João". */
    static final Set<String> SUFIXOS_PARENTESCO = Set.of("filho", "filha", "júnior", "junior", "neto", "neta", "sobrinho", "sobrinha");

    private NomeAbnt() {
    }

    /**
     * Inverte um nome em ordem direta para a entrada ABNT.
     * <ul>
     *   <li>"Machado de Assis" → "ASSIS, Machado de"</li>
     *   <li>"João Silva Filho" → "SILVA FILHO, João"</li>
     *   <li>"Aristóteles" → "ARISTÓTELES"</li>
     * </ul>
     */
    public static String inverter(String nomeCompleto) {
        var partes = List.of(nomeCompleto.trim().split("\\s+"));
        if (partes.size() == 1) {
            return partes.getFirst().toUpperCase(PT_BR);
        }
        // O sufixo só arrasta o sobrenome anterior se ainda sobrar um prenome: "Paulo Neto" → "NETO, Paulo"
        var comSufixo = partes.size() > 2 && SUFIXOS_PARENTESCO.contains(partes.getLast().toLowerCase(PT_BR));
        var inicioSobrenome = partes.size() - (comSufixo ? 2 : 1);
        var sobrenome = String.join(" ", partes.subList(inicioSobrenome, partes.size())).toUpperCase(PT_BR);
        return sobrenome + ", " + String.join(" ", partes.subList(0, inicioSobrenome));
    }

    /** Só o sobrenome de entrada ("ASSIS"), usado na citação autor-data. */
    public static String sobrenome(String nomeCompleto) {
        var invertido = inverter(nomeCompleto);
        var virgula = invertido.indexOf(',');
        return virgula < 0 ? invertido : invertido.substring(0, virgula);
    }
}
