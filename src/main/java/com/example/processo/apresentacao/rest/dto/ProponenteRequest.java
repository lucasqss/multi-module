package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Cpf;

/** CPF só com os 11 dígitos. */
public record ProponenteRequest(String nome, String cpf) {

    /**
     * Sem proponente no corpo, o CPF segue nulo: quem recusa é a regra comum do nome, com a mensagem dela. CPF inválido
     * já para aqui, ao virar {@code Cpf} (422 com o motivo).
     */
    Cpf paraCpf() {
        return nome == null && cpf == null ? null : new Cpf(cpf);
    }
}
