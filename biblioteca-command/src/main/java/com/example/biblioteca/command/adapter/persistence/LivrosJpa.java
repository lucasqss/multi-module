package com.example.biblioteca.command.adapter.persistence;

import com.example.biblioteca.command.domain.Livro;
import com.example.biblioteca.command.domain.Livros;
import com.example.jpacore.AutoriaEntity;
import com.example.jpacore.LivroEntity;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Adapter do port {@link Livros}. A implementação dos métodos anotados é gerada pelo Hibernate Processor
 * (LivrosJpa_), que valida o HQL contra as entidades do jpa-core em tempo de compilação.
 */
@Repository
public interface LivrosJpa extends Livros {

    @Insert
    void inserir(LivroEntity livro);

    @Insert
    void inserir(List<AutoriaEntity> autorias);

    @Query("select count(*) from LivroEntity where isbn = :isbn")
    long contarPorIsbn(String isbn);

    @Query("select count(*) from LivroEntity where id = :id")
    long contarPorId(UUID id);

    @Override
    default void salvar(Livro livro) {
        var entity = new LivroEntity(livro.id(), livro.isbn(), livro.titulo(), livro.subtitulo(),
                livro.edicao(), livro.local(), livro.editora(), livro.ano());
        inserir(entity);
        // Sessão stateless não faz cascade: cada linha é gravada explicitamente
        inserir(IntStream.range(0, livro.autores().size())
                .mapToObj(i -> new AutoriaEntity(UUID.randomUUID(), livro.id(), i, livro.autores().get(i)))
                .toList());
    }

    @Override
    default boolean existeIsbn(String isbn) {
        return contarPorIsbn(isbn) > 0;
    }

    @Override
    default boolean existe(UUID id) {
        return contarPorId(id) > 0;
    }
}
