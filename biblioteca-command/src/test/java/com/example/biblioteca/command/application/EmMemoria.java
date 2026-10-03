package com.example.biblioteca.command.application;

import com.example.biblioteca.command.domain.Emprestimo;
import com.example.biblioteca.command.domain.Emprestimos;
import com.example.biblioteca.command.domain.Exemplar;
import com.example.biblioteca.command.domain.Exemplares;
import com.example.biblioteca.command.domain.Leitor;
import com.example.biblioteca.command.domain.Leitores;
import com.example.biblioteca.command.domain.Livro;
import com.example.biblioteca.command.domain.Livros;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Fakes dos ports: os casos de uso rodam sem Quarkus e sem banco. */
class EmMemoria {

    static class LivrosEmMemoria implements Livros {
        final Map<UUID, Livro> dados = new HashMap<>();

        public void salvar(Livro livro) { dados.put(livro.id(), livro); }
        public boolean existeIsbn(String isbn) { return dados.values().stream().anyMatch(l -> l.isbn().equals(isbn)); }
        public boolean existe(UUID id) { return dados.containsKey(id); }
    }

    static class ExemplaresEmMemoria implements Exemplares {
        final Map<UUID, Exemplar> dados = new HashMap<>();

        public Optional<Exemplar> porId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public boolean existeTombo(String tombo) { return dados.values().stream().anyMatch(e -> e.tombo().equals(tombo)); }
        public void salvar(Exemplar exemplar) { dados.put(exemplar.id(), exemplar); }
    }

    static class LeitoresEmMemoria implements Leitores {
        final Map<UUID, Leitor> dados = new HashMap<>();

        public Optional<Leitor> porId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public boolean existeEmail(String email) { return dados.values().stream().anyMatch(l -> l.email().equals(email)); }
        public void salvar(Leitor leitor) { dados.put(leitor.id(), leitor); }
    }

    static class EmprestimosEmMemoria implements Emprestimos {
        final Map<UUID, Emprestimo> dados = new HashMap<>();

        public Optional<Emprestimo> porId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public List<Emprestimo> abertosDo(UUID leitorId) {
            return dados.values().stream().filter(e -> e.leitorId().equals(leitorId) && e.aberto()).toList();
        }
        public void salvar(Emprestimo emprestimo) { dados.put(emprestimo.id(), emprestimo); }
    }
}
