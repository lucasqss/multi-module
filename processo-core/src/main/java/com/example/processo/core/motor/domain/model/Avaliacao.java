package com.example.processo.core.motor.domain.model;

/** Decisão do segmento sobre um processo em processamento. O motor aplica a transição correspondente. */
public record Avaliacao(boolean aprovada, String motivo) {

    public static Avaliacao aprovado() {
        return new Avaliacao(true, null);
    }

    public static Avaliacao recusado(String motivo) {
        return new Avaliacao(false, motivo);
    }
}
