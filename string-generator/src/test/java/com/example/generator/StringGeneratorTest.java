package com.example.generator;

import com.example.crud.Produto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringGeneratorTest {

    @Test
    void concatenaNomeEDescricao() {
        Produto a = new Produto();
        a.nome = "A";
        a.descricao = "1";
        Produto b = new Produto();
        b.nome = "B";

        assertEquals("A1B", new StringGenerator().concatenar(List.of(a, b)));
    }
}
