package com.example.processo.negocio.processo;

import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.Valor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** O que sobrou para as regras comuns: CPF e valor inválidos nem chegam aqui (value objects). */
class RegrasComunsTest {

    static Optional<String> violacao(String nome, Valor valor) {
        return RegrasComuns.TODAS.violacao(new ProcessoVeicular(nome, new Cpf("52998224725"), valor, null, null, null));
    }

    @Test
    void nomeObrigatorio() {
        var valor = new Valor("1000");
        assertEquals(Optional.of("Nome do proponente é obrigatório"), violacao(null, valor));
        assertEquals(Optional.of("Nome do proponente é obrigatório"), violacao(" ", valor));
        assertEquals(Optional.empty(), violacao("Ana Souza", valor));
    }

    @Test
    void valorObrigatorio() {
        assertEquals(Optional.of("Valor do processo deve ser maior que zero"), violacao("Ana Souza", null));
    }
}
