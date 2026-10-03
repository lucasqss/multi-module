package com.example.biblioteca.command.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public record Emprestimo(UUID id, UUID leitorId, UUID exemplarId, LocalDate data, LocalDate dataPrevista,
                         int renovacoes, LocalDate dataDevolucao) {

    public static final int PRAZO_DIAS = 14;
    public static final int MAX_RENOVACOES = 2;

    /** Regra sobre o empréstimo num determinado dia. */
    public record NoDia(Emprestimo emprestimo, LocalDate hoje) {
    }

    static final Especificacao<NoDia> ABERTO = c -> c.emprestimo().aberto()
            ? Optional.empty()
            : Optional.of("Empréstimo já foi devolvido");

    static final Especificacao<NoDia> SEM_ATRASO = c -> c.emprestimo().atrasadoEm(c.hoje())
            ? Optional.of("Empréstimo atrasado não pode ser renovado")
            : Optional.empty();

    static final Especificacao<NoDia> ABAIXO_DO_LIMITE_DE_RENOVACOES = c -> c.emprestimo().renovacoes() < MAX_RENOVACOES
            ? Optional.empty()
            : Optional.of("Limite de " + MAX_RENOVACOES + " renovações atingido");

    static final Especificacao<NoDia> PODE_RENOVAR = ABERTO.e(SEM_ATRASO).e(ABAIXO_DO_LIMITE_DE_RENOVACOES);

    public static Emprestimo realizar(UUID leitorId, UUID exemplarId, LocalDate hoje) {
        return new Emprestimo(UUID.randomUUID(), leitorId, exemplarId, hoje, hoje.plusDays(PRAZO_DIAS), 0, null);
    }

    public boolean aberto() {
        return dataDevolucao == null;
    }

    public boolean atrasadoEm(LocalDate dia) {
        return aberto() && dia.isAfter(dataPrevista);
    }

    public long diasDeAtrasoEm(LocalDate dia) {
        return Math.max(0, ChronoUnit.DAYS.between(dataPrevista, dia));
    }

    /** O novo prazo conta a partir do dia da renovação. */
    public Emprestimo renovar(LocalDate hoje) {
        PODE_RENOVAR.exigir(new NoDia(this, hoje));
        return new Emprestimo(id, leitorId, exemplarId, data, hoje.plusDays(PRAZO_DIAS), renovacoes + 1, null);
    }

    public Emprestimo devolver(LocalDate hoje) {
        ABERTO.exigir(new NoDia(this, hoje));
        return new Emprestimo(id, leitorId, exemplarId, data, dataPrevista, renovacoes, hoje);
    }
}
