package com.example.processo.integracoes.cadastro.dto;

/** Modelo do sistema de cadastro, como ele responde. Não sai do pacote cadastro: o mapper converte para o domínio. */
public record RespostaCadastro(String documento, String situacao) {

    public static final String REGULAR = "REGULAR";
}
