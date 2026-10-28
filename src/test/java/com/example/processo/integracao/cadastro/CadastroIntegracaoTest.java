package com.example.processo.integracao.cadastro;

import com.example.processo.modelo.SistemaExternoIndisponivelException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A tradução da resposta externa; o cliente é substituído por um que responde fixo. A chamada HTTP real é coberta nos ITs. */
class CadastroIntegracaoTest {

    static CadastroIntegracao respondendo(String situacao) {
        return new CadastroIntegracao(documento -> new RespostaCadastro(documento, situacao));
    }

    @Test
    void regularSoQuandoOSistemaRespondeRegular() {
        assertTrue(respondendo("REGULAR").regular("52998224725"));
        assertFalse(respondendo("SUSPENSA").regular("52998224725"));
        assertFalse(respondendo(null).regular("52998224725"));
    }

    @Test
    void simulacaoDoDevSempreRespondeRegular() {
        assertTrue(new CadastroIntegracao(new CadastroClientSimulado()).regular("52998224725"));
    }

    @Test
    void falhaDoClienteViraIndisponibilidade() {
        var foraDoAr = new CadastroIntegracao(documento -> {
            throw new ProcessingException("Connection refused");
        });
        var erroHttp = new CadastroIntegracao(documento -> {
            throw new WebApplicationException(500);
        });

        assertThrows(SistemaExternoIndisponivelException.class, () -> foraDoAr.regular("52998224725"));
        assertThrows(SistemaExternoIndisponivelException.class, () -> erroHttp.regular("52998224725"));
    }
}
