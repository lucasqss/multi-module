package com.example.processo.core.motor.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasComunsTest {

    record Dados() implements DadosDoSegmento {
    }

    static Optional<String> violacao(String nome, String cpf, String valor) {
        var solicitacao = new Solicitacao<>(new Proponente(nome, cpf), valor == null ? null : new BigDecimal(valor),
                new Dados());
        return RegrasComuns.<Dados>todas().violacao(solicitacao);
    }

    @Test
    void cpfComDigitosVerificadoresCorretos() {
        assertTrue(RegrasComuns.cpfValido("52998224725"));
        assertTrue(RegrasComuns.cpfValido("11144477735"));
    }

    @Test
    void cpfInvalido() {
        assertFalse(RegrasComuns.cpfValido("52998224724"));  // 2º dígito verificador errado
        assertFalse(RegrasComuns.cpfValido("52998224735"));  // 1º dígito verificador errado
        assertFalse(RegrasComuns.cpfValido("11111111111"));  // todos iguais passam no cálculo, mas são inválidos
        assertFalse(RegrasComuns.cpfValido("529.982.247-25"));
        assertFalse(RegrasComuns.cpfValido(null));
    }

    @Test
    void solicitacaoValida() {
        assertEquals(Optional.empty(), violacao("Ana Souza", "52998224725", "100000.00"));
    }

    @Test
    void motivoDaPrimeiraRegraViolada() {
        assertEquals(Optional.of("Nome do proponente é obrigatório"), violacao(" ", "000", "0"));
        assertEquals(Optional.of("CPF inválido: 000"), violacao("Ana Souza", "000", "0"));
        assertEquals(Optional.of("Valor do processo deve ser maior que zero"), violacao("Ana Souza", "52998224725", "0"));
        assertEquals(Optional.of("Valor do processo deve ser maior que zero"), violacao("Ana Souza", "52998224725", null));
    }
}
