package com.example.biblioteca.command.application;

import com.example.biblioteca.command.domain.Exemplar;
import com.example.biblioteca.command.domain.Exemplares;
import com.example.biblioteca.command.domain.Livro;
import com.example.biblioteca.command.domain.Livros;
import com.example.biblioteca.command.domain.NaoEncontradoException;
import com.example.biblioteca.command.domain.RegraVioladaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

/** Casos de uso do acervo: livros e seus exemplares. */
@ApplicationScoped
public class AcervoService {

    public record NovoLivro(String isbn, String titulo, String subtitulo, List<String> autores,
                            int edicao, String local, String editora, int ano) {
    }

    private final Livros livros;
    private final Exemplares exemplares;

    public AcervoService(Livros livros, Exemplares exemplares) {
        this.livros = livros;
        this.exemplares = exemplares;
    }

    @Transactional
    public UUID cadastrarLivro(NovoLivro c) {
        if (livros.existeIsbn(c.isbn())) {
            throw new RegraVioladaException("Já existe livro com ISBN " + c.isbn());
        }
        var livro = Livro.cadastrar(c.isbn(), c.titulo(), c.subtitulo(), c.autores(),
                c.edicao(), c.local(), c.editora(), c.ano());
        livros.salvar(livro);
        return livro.id();
    }

    @Transactional
    public UUID registrarExemplar(UUID livroId, String tombo) {
        if (!livros.existe(livroId)) {
            throw new NaoEncontradoException("Livro", livroId);
        }
        if (exemplares.existeTombo(tombo)) {
            throw new RegraVioladaException("Já existe exemplar com tombo " + tombo);
        }
        var exemplar = Exemplar.registrar(livroId, tombo);
        exemplares.salvar(exemplar);
        return exemplar.id();
    }

    @Transactional
    public void baixarExemplar(UUID exemplarId) {
        var exemplar = exemplares.porId(exemplarId).orElseThrow(() -> new NaoEncontradoException("Exemplar", exemplarId));
        exemplares.salvar(exemplar.baixar());
    }
}
