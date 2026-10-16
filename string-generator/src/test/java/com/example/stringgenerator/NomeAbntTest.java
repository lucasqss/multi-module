package com.example.stringgenerator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NomeAbntTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(delimiter = '|', textBlock = """
            Graciliano Ramos            | RAMOS, Graciliano
            Machado de Assis            | ASSIS, Machado de
            Carlos Drummond de Andrade  | ANDRADE, Carlos Drummond de
            Érico Veríssimo             | VERÍSSIMO, Érico
            João Silva Filho            | SILVA FILHO, João
            Hélio Gomes Neto            | GOMES NETO, Hélio
            Paulo Neto                  | NETO, Paulo
            Aristóteles                 | ARISTÓTELES
            '  Machado   de  Assis  '   | ASSIS, Machado de
            """)
    void inverteParaEntradaAbnt(String nomeCompleto, String esperado) {
        assertEquals(esperado, NomeAbnt.inverter(nomeCompleto));
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(delimiter = '|', textBlock = """
            Machado de Assis  | ASSIS
            João Silva Filho  | SILVA FILHO
            Aristóteles       | ARISTÓTELES
            """)
    void extraiSobrenomeDeEntrada(String nomeCompleto, String esperado) {
        assertEquals(esperado, NomeAbnt.sobrenome(nomeCompleto));
    }
}
