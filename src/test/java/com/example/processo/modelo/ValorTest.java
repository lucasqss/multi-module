package com.example.processo.modelo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValorTest {

    @Test
    void escalaSempreDois() {
        // O Oracle devolve 400000 para 400000.00: com a escala fixa, os dois são o mesmo valor
        assertEquals(new Valor("400000.00"), new Valor(new BigDecimal("400000")));
        assertEquals("400000.50", new Valor("400000.5").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.00"})
    void maiorQueZero(String quantia) {
        var erro = assertThrows(RegraVioladaException.class, () -> new Valor(quantia));
        assertEquals("Valor do processo deve ser maior que zero", erro.getMessage());
    }

    @Test
    void semValorNaoExiste() {
        assertThrows(RegraVioladaException.class, () -> new Valor((BigDecimal) null));
    }

    @Test
    void noMaximoDuasCasas() {
        var erro = assertThrows(RegraVioladaException.class, () -> new Valor("10.001"));
        assertEquals("Valor deve ter no máximo 2 casas decimais", erro.getMessage());
    }
}
