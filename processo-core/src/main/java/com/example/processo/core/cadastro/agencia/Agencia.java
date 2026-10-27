package com.example.processo.core.cadastro.agencia;

import com.example.processo.core.motor.domain.exception.RegraVioladaException;

/** Agência, válida por construção: inválida dá 422 com o motivo, sem uma camada de regras à parte. */
public record Agencia(String codigo, String nome, String uf, boolean ativa) {

    public Agencia {
        if (codigo == null || !codigo.matches("\\d{4}")) {
            throw new RegraVioladaException("Código da agência deve ter 4 dígitos: " + codigo);
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("Nome da agência é obrigatório");
        }
        if (uf == null || !uf.matches("[A-Z]{2}")) {
            throw new RegraVioladaException("UF inválida: " + uf);
        }
    }
}
