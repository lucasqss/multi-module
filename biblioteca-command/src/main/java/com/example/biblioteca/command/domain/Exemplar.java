package com.example.biblioteca.command.domain;

import java.util.Optional;
import java.util.UUID;

/** Cópia física de um {@link Livro}, identificada pelo tombo (número de patrimônio). */
public record Exemplar(UUID id, UUID livroId, String tombo, Situacao situacao) {

    public enum Situacao { DISPONIVEL, EMPRESTADO, BAIXADO }

    static final Especificacao<Exemplar> DISPONIVEL_PARA_EMPRESTIMO = e -> e.situacao() == Situacao.DISPONIVEL
            ? Optional.empty()
            : Optional.of("Exemplar " + e.tombo() + " não está disponível (" + e.situacao() + ")");

    static final Especificacao<Exemplar> PODE_SER_BAIXADO = e -> e.situacao() == Situacao.EMPRESTADO
            ? Optional.of("Exemplar " + e.tombo() + " está emprestado e não pode ser baixado")
            : Optional.empty();

    public static Exemplar registrar(UUID livroId, String tombo) {
        if (tombo == null || tombo.isBlank()) {
            throw new RegraVioladaException("Tombo é obrigatório");
        }
        return new Exemplar(UUID.randomUUID(), livroId, tombo, Situacao.DISPONIVEL);
    }

    public Exemplar emprestar() {
        DISPONIVEL_PARA_EMPRESTIMO.exigir(this);
        return comSituacao(Situacao.EMPRESTADO);
    }

    public Exemplar devolver() {
        return comSituacao(Situacao.DISPONIVEL);
    }

    public Exemplar baixar() {
        PODE_SER_BAIXADO.exigir(this);
        return comSituacao(Situacao.BAIXADO);
    }

    private Exemplar comSituacao(Situacao nova) {
        return new Exemplar(id, livroId, tombo, nova);
    }
}
