package com.example.processo.integracoes.cadastro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A tradução da resposta externa; o cliente é substituído por um que responde fixo. A chamada HTTP real é coberta nos ITs. */
class SituacaoCadastralAdapterTest {

    static SituacaoCadastralAdapter respondendo(String situacao) {
        return new SituacaoCadastralAdapter(documento -> new RespostaCadastro(documento, situacao));
    }

    @Test
    void regularSoQuandoOSistemaRespondeRegular() {
        assertTrue(respondendo("REGULAR").regular("52998224725"));
        assertFalse(respondendo("SUSPENSA").regular("52998224725"));
        assertFalse(respondendo(null).regular("52998224725"));
    }

    @Test
    void simulacaoDoDevSempreRespondeRegular() {
        assertTrue(new SituacaoCadastralAdapter(new CadastroClientSimulado()).regular("52998224725"));
    }
}
