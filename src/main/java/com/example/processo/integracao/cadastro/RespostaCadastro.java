package com.example.processo.integracao.cadastro;

/** Modelo do sistema de cadastro, como ele responde. Não sai do pacote cadastro: a integração só usa {@link #regular()}. */
public record RespostaCadastro(String documento, String situacao) {

    public static final String REGULAR = "REGULAR";

    /** Só a situação REGULAR conta como regular; qualquer outra (ou nenhuma) não. */
    public boolean regular() {
        return REGULAR.equals(situacao);
    }
}
