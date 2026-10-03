package com.example.biblioteca.command.application;

import com.example.biblioteca.command.domain.Emprestimo;
import com.example.biblioteca.command.domain.Emprestimos;
import com.example.biblioteca.command.domain.Exemplares;
import com.example.biblioteca.command.domain.Leitor;
import com.example.biblioteca.command.domain.LeitorPodeEmprestar;
import com.example.biblioteca.command.domain.Leitores;
import com.example.biblioteca.command.domain.NaoEncontradoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/** Circulação: empréstimo, renovação e devolução. Orquestra carregar → regra → salvar. */
@ApplicationScoped
public class CirculacaoService {

    private final Leitores leitores;
    private final Exemplares exemplares;
    private final Emprestimos emprestimos;
    private final Clock relogio;
    private final LeitorPodeEmprestar leitorPodeEmprestar = new LeitorPodeEmprestar();

    public CirculacaoService(Leitores leitores, Exemplares exemplares, Emprestimos emprestimos, Clock relogio) {
        this.leitores = leitores;
        this.exemplares = exemplares;
        this.emprestimos = emprestimos;
        this.relogio = relogio;
    }

    @Transactional
    public UUID emprestar(UUID leitorId, UUID exemplarId) {
        var hoje = hoje();
        var leitor = leitor(leitorId);
        var exemplar = exemplares.porId(exemplarId).orElseThrow(() -> new NaoEncontradoException("Exemplar", exemplarId));

        leitorPodeEmprestar.exigir(new LeitorPodeEmprestar.Situacao(leitor, emprestimos.abertosDo(leitorId), hoje));
        var emprestado = exemplar.emprestar();
        var emprestimo = Emprestimo.realizar(leitorId, exemplarId, hoje);

        exemplares.salvar(emprestado);
        emprestimos.salvar(emprestimo);
        return emprestimo.id();
    }

    @Transactional
    public LocalDate renovar(UUID emprestimoId) {
        var renovado = emprestimo(emprestimoId).renovar(hoje());
        emprestimos.salvar(renovado);
        return renovado.dataPrevista();
    }

    @Transactional
    public void devolver(UUID emprestimoId) {
        var hoje = hoje();
        var emprestimo = emprestimo(emprestimoId);
        var devolvido = emprestimo.devolver(hoje);
        exemplares.porId(emprestimo.exemplarId()).ifPresent(e -> exemplares.salvar(e.devolver()));
        if (emprestimo.atrasadoEm(hoje)) {
            leitores.salvar(leitor(emprestimo.leitorId()).suspenderPorAtraso(emprestimo.diasDeAtrasoEm(hoje), hoje));
        }
        emprestimos.salvar(devolvido);
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    private Leitor leitor(UUID id) {
        return leitores.porId(id).orElseThrow(() -> new NaoEncontradoException("Leitor", id));
    }

    private Emprestimo emprestimo(UUID id) {
        return emprestimos.porId(id).orElseThrow(() -> new NaoEncontradoException("Empréstimo", id));
    }
}
