package com.example.biblioteca.command.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmprestimoTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final Emprestimo emprestimo = Emprestimo.realizar(UUID.randomUUID(), UUID.randomUUID(), HOJE);

    @Test
    void prazoDeQuatorzeDias() {
        assertEquals(HOJE.plusDays(14), emprestimo.dataPrevista());
        assertFalse(emprestimo.atrasadoEm(HOJE.plusDays(14)));
        assertTrue(emprestimo.atrasadoEm(HOJE.plusDays(15)));
    }

    @Test
    void renovarContaNovoPrazoAPartirDoDiaDaRenovacao() {
        var renovado = emprestimo.renovar(HOJE.plusDays(10));
        assertEquals(HOJE.plusDays(24), renovado.dataPrevista());
        assertEquals(1, renovado.renovacoes());
    }

    @Test
    void naoRenovaMaisQueDuasVezes() {
        var duasVezes = emprestimo.renovar(HOJE).renovar(HOJE);
        var erro = assertThrows(RegraVioladaException.class, () -> duasVezes.renovar(HOJE));
        assertEquals("Limite de 2 renovações atingido", erro.getMessage());
    }

    @Test
    void naoRenovaEmprestimoAtrasado() {
        var erro = assertThrows(RegraVioladaException.class, () -> emprestimo.renovar(HOJE.plusDays(15)));
        assertEquals("Empréstimo atrasado não pode ser renovado", erro.getMessage());
    }

    @Test
    void naoDevolveDuasVezes() {
        var devolvido = emprestimo.devolver(HOJE);
        assertFalse(devolvido.aberto());
        assertThrows(RegraVioladaException.class, () -> devolvido.devolver(HOJE));
        assertThrows(RegraVioladaException.class, () -> devolvido.renovar(HOJE));
    }

    @Test
    void diasDeAtraso() {
        assertEquals(0, emprestimo.diasDeAtrasoEm(HOJE.plusDays(14)));
        assertEquals(3, emprestimo.diasDeAtrasoEm(HOJE.plusDays(17)));
    }
}
