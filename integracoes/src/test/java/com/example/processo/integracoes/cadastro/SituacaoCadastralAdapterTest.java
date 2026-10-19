package com.example.processo.integracoes.cadastro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O que interessa testar aqui é a tradução da resposta externa; o cliente é substituído por um que responde fixo. */
class SituacaoCadastralAdapterTest {

    static SituacaoCadastralAdapter respondendo(String situacao) {
        return new SituacaoCadastralAdapter(new CadastroClient() {
            @Override
            public RespostaCadastro consultar(String documento) {
                return new RespostaCadastro(documento, situacao);
            }
        });
    }

    @Test
    void regularSoQuandoOSistemaRespondeRegular() {
        assertTrue(respondendo("REGULAR").regular("52998224725"));
        assertFalse(respondendo("SUSPENSA").regular("52998224725"));
        assertFalse(respondendo(null).regular("52998224725"));
    }

    @Test
    void clienteSimuladoSempreRespondeRegular() {
        assertTrue(new SituacaoCadastralAdapter(new CadastroClient()).regular("52998224725"));
    }
}
