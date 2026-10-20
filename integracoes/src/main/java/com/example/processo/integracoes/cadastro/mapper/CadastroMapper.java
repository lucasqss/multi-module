package com.example.processo.integracoes.cadastro.mapper;

import com.example.processo.integracoes.cadastro.dto.RespostaCadastro;

/** Modelo do sistema de cadastro → o que o domínio entende (anticorrupção). */
public final class CadastroMapper {

    private CadastroMapper() {
    }

    /** Só a situação REGULAR conta como regular; qualquer outra (ou nenhuma) não. */
    public static boolean regular(RespostaCadastro resposta) {
        return RespostaCadastro.REGULAR.equals(resposta.situacao());
    }
}
