package com.example.processo.integracoes.cadastro;

/** Modelo do sistema de cadastro, como ele responde. Não sai deste pacote: o adapter converte para o domínio. */
public record RespostaCadastro(String documento, String situacao) {

    static final String REGULAR = "REGULAR";
}
