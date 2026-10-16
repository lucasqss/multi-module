package com.example.biblioteca.command.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeitorPodeEmprestarTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 1);

    private final LeitorPodeEmprestar regra = new LeitorPodeEmprestar();
    private final Leitor leitor = Leitor.cadastrar("Ana Souza", "ana@example.com");

    private static Emprestimo emDia() {
        return Emprestimo.realizar(UUID.randomUUID(), UUID.randomUUID(), HOJE.minusDays(1));
    }

    private static Emprestimo atrasado() {
        return Emprestimo.realizar(UUID.randomUUID(), UUID.randomUUID(), HOJE.minusDays(20));
    }

    private Optional<String> avaliar(Leitor l, List<Emprestimo> abertos) {
        return regra.violacao(new LeitorPodeEmprestar.Situacao(l, abertos, HOJE));
    }

    @Test
    void leitorSemPendenciasPodeEmprestar() {
        assertEquals(Optional.empty(), avaliar(leitor, List.of()));
        assertEquals(Optional.empty(), avaliar(leitor, List.of(emDia(), emDia())));
    }

    @Test
    void leitorSuspensoNaoPodeEmprestar() {
        var suspenso = leitor.suspenderPorAtraso(2, HOJE.minusDays(1));
        assertEquals(Optional.of("Leitor suspenso até " + HOJE.plusDays(1)), avaliar(suspenso, List.of()));
    }

    @Test
    void suspensaoVencidaNaoImpede() {
        var suspensaoAntiga = leitor.suspenderPorAtraso(2, HOJE.minusDays(10));
        assertEquals(Optional.empty(), avaliar(suspensaoAntiga, List.of()));
    }

    @Test
    void limiteDeTresEmprestimosAbertos() {
        assertEquals(Optional.of("Limite de 3 empréstimos abertos atingido"),
                avaliar(leitor, Collections.nCopies(3, emDia())));
    }

    @Test
    void emprestimoAtrasadoImpede() {
        assertEquals(Optional.of("Leitor possui empréstimo atrasado"), avaliar(leitor, List.of(atrasado())));
    }

    @Test
    void comVariasViolacoesInformaPrimeiroOAtraso() {
        assertEquals(Optional.of("Leitor possui empréstimo atrasado"),
                avaliar(leitor, List.of(emDia(), emDia(), atrasado())));
    }

    @Test
    void suspensaoTemPrioridadeSobreAsDemais() {
        var suspenso = leitor.suspenderPorAtraso(2, HOJE);
        assertEquals(Optional.of("Leitor suspenso até " + HOJE.plusDays(2)),
                avaliar(suspenso, List.of(emDia(), emDia(), atrasado())));
    }
}
