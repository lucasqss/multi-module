package com.example.processo.integracoes.cadastro.adapter;

import com.example.processo.core.motor.domain.exception.SistemaExternoIndisponivelException;
import com.example.processo.core.motor.domain.model.Cpf;
import com.example.processo.integracoes.cadastro.client.CadastroClientSimulado;
import com.example.processo.integracoes.cadastro.dto.RespostaCadastro;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A tradução da resposta externa; o cliente é substituído por um que responde fixo. A chamada HTTP real é coberta nos ITs. */
class SituacaoCadastralAdapterTest {

    static SituacaoCadastralAdapter respondendo(String situacao) {
        return new SituacaoCadastralAdapter(documento -> new RespostaCadastro(documento, situacao));
    }

    @Test
    void regularSoQuandoOSistemaRespondeRegular() {
        assertTrue(respondendo("REGULAR").regular(new Cpf("52998224725")));
        assertFalse(respondendo("SUSPENSA").regular(new Cpf("52998224725")));
        assertFalse(respondendo(null).regular(new Cpf("52998224725")));
    }

    @Test
    void simulacaoDoDevSempreRespondeRegular() {
        assertTrue(new SituacaoCadastralAdapter(new CadastroClientSimulado()).regular(new Cpf("52998224725")));
    }

    @Test
    void falhaDoClienteViraIndisponibilidadeDoDominio() {
        var foraDoAr = new SituacaoCadastralAdapter(documento -> {
            throw new ProcessingException("Connection refused");
        });
        var erroHttp = new SituacaoCadastralAdapter(documento -> {
            throw new WebApplicationException(500);
        });

        assertThrows(SistemaExternoIndisponivelException.class, () -> foraDoAr.regular(new Cpf("52998224725")));
        assertThrows(SistemaExternoIndisponivelException.class, () -> erroHttp.regular(new Cpf("52998224725")));
    }
}
