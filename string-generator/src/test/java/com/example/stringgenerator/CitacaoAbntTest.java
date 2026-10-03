package com.example.stringgenerator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CitacaoAbntTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource(delimiter = '|', textBlock = """
            Machado de Assis                                                    | (ASSIS, 1899)
            Machado de Assis;José de Alencar                                    | (ASSIS; ALENCAR, 1899)
            Machado de Assis;José de Alencar;João Silva Filho                   | (ASSIS; ALENCAR; SILVA FILHO, 1899)
            Machado de Assis;José de Alencar;Graciliano Ramos;Érico Veríssimo   | (ASSIS et al., 1899)
            """)
    void citacaoAutorData(String autores, String esperado) {
        var obra = new ObraRef(Arrays.stream(autores.split(";")).map(AutorRef::new).toList(),
                "Dom Casmurro", null, 1, null, null, 1899);
        assertEquals(esperado, CitacaoAbnt.de(obra));
    }
}
