package com.example.processo.modelo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Válido por construção: só existe {@code Cpf} com dígitos verificadores corretos. */
class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "11144477735"})
    void validos(String numero) {
        assertEquals(numero, new Cpf(numero).toString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "52998224724",    // 2º dígito verificador errado
            "52998224735",    // 1º dígito verificador errado
            "11111111111",    // todos iguais passam no cálculo, mas são inválidos
            "529.982.247-25", // só dígitos
            "123"})
    void invalidosNemExistem(String numero) {
        var erro = assertThrows(RegraVioladaException.class, () -> new Cpf(numero));
        assertEquals("CPF inválido: " + numero, erro.getMessage());
    }
}
