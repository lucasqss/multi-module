package com.example.modelo.core.interno;

/**
 * Detalhe de implementação do modelo-core. Precisa ser público para ser usado de outro pacote do próprio
 * módulo, mas pacotes {@code interno} não fazem parte da API: o ArchUnit (ArquiteturaTest, no modelo-app)
 * impede que outro módulo os use.
 */
public final class Nomes {

    private Nomes() {
    }

    public static String limpar(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome é obrigatório");
        }
        return nome.strip().replaceAll("\\s+", " ");
    }
}
