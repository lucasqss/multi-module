package com.example.processo.apresentacao.rest.dto;

import com.example.processo.modelo.Agencia;

/** Agência como a API devolve: a entidade não sai na resposta, para o schema poder mudar sem mudar o contrato. */
public record AgenciaResponse(String codigo, String nome, String uf, boolean ativa) {

    public static AgenciaResponse de(Agencia agencia) {
        return new AgenciaResponse(agencia.getCodigo(), agencia.getNome(), agencia.getUf(), agencia.isAtiva());
    }
}
