package com.example.processo.integracoes.cadastro.dto;

/** Modelo do sistema de cadastro, como ele responde. Não sai do pacote cadastro: o adapter só usa {@link #regular()}. */
public record RespostaCadastro(String documento, String situacao) {

    public static final String REGULAR = "REGULAR";

    /** Só a situação REGULAR conta como regular; qualquer outra (ou nenhuma) não. */
    public boolean regular() {
        return REGULAR.equals(situacao);
    }
}
