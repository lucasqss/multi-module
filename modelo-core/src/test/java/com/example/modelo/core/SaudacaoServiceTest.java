package com.example.modelo.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SaudacaoServiceTest {

    private final SaudacaoService service = new SaudacaoService(() -> "Oi");

    @Test
    void montaMensagemComPrefixoConfigurado() {
        assertEquals(new Saudacao("Ana", "Oi, Ana!"), service.saudar("  Ana "));
    }

    @Test
    void juntaEspacosRepetidosNoNome() {
        assertEquals(new Saudacao("Ana Maria", "Oi, Ana Maria!"), service.saudar("Ana   Maria"));
    }

    @Test
    void rejeitaNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> service.saudar(" "));
    }
}
