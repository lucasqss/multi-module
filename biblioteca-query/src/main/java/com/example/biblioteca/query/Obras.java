package com.example.biblioteca.query;

import com.example.jpacore.AutoriaEntity;
import com.example.jpacore.EmprestimoEntity;
import com.example.jpacore.LivroEntity;
import com.example.stringgenerator.AutorRef;
import com.example.stringgenerator.ObraRef;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/** Traduz o read model (entidades do jpa-core) para a entrada do string-generator. */
@ApplicationScoped
public class Obras {

    private final AcervoQueries acervo;

    Obras(AcervoQueries acervo) {
        this.acervo = acervo;
    }

    /** Agrupa linhas de autoria por livro, preservando a ordem da consulta. Agrupa pelo id: sessão stateless
     *  não garante a mesma instância de LivroEntity entre linhas. */
    static Map<UUID, List<AutoriaEntity>> porLivro(List<AutoriaEntity> autorias) {
        return autorias.stream().collect(groupingBy(AutoriaEntity::getLivroId, LinkedHashMap::new, toList()));
    }

    static ObraRef obra(List<AutoriaEntity> autoriasDoLivro) {
        LivroEntity l = autoriasDoLivro.getFirst().getLivro();
        var autores = autoriasDoLivro.stream().map(a -> new AutorRef(a.getNome())).toList();
        return new ObraRef(autores, l.getTitulo(), l.getSubtitulo(), l.getEdicao(), l.getLocal(), l.getEditora(), l.getAno());
    }

    /** Obras dos livros emprestados, indexadas pelo id do livro. */
    Map<UUID, ObraRef> dosEmprestimos(List<EmprestimoEntity> emprestimos) {
        return dos(emprestimos.stream().map(e -> e.getExemplar().getLivroId()).distinct().toList());
    }

    private Map<UUID, ObraRef> dos(Collection<UUID> livroIds) {
        if (livroIds.isEmpty()) {
            return Map.of();
        }
        var obras = new LinkedHashMap<UUID, ObraRef>();
        porLivro(acervo.autoriasDos(livroIds)).forEach((id, autorias) -> obras.put(id, obra(autorias)));
        return obras;
    }
}
