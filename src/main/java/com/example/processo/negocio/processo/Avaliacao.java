package com.example.processo.negocio.processo;

/** Decisão da modalidade sobre um processo em processamento. O serviço aplica a transição correspondente. */
public record Avaliacao(boolean aprovada, String motivo) {

    public static Avaliacao aprovado() {
        return new Avaliacao(true, null);
    }

    public static Avaliacao recusado(String motivo) {
        return new Avaliacao(false, motivo);
    }
}
