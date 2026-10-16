package com.example.biblioteca.command.adapter.persistence;

import com.example.biblioteca.command.domain.Exemplar;
import com.example.biblioteca.command.domain.Exemplares;
import com.example.jpacore.ExemplarEntity;
import com.example.jpacore.SituacaoExemplar;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExemplaresJpa extends Exemplares {

    @Find
    Optional<ExemplarEntity> buscar(UUID id);

    @Query("select count(*) from ExemplarEntity where tombo = :tombo")
    long contarPorTombo(String tombo);

    @Save
    void gravar(ExemplarEntity exemplar);

    @Override
    default Optional<Exemplar> porId(UUID id) {
        return buscar(id).map(e -> new Exemplar(e.getId(), e.getLivroId(), e.getTombo(),
                Exemplar.Situacao.valueOf(e.getSituacao().name())));
    }

    @Override
    default boolean existeTombo(String tombo) {
        return contarPorTombo(tombo) > 0;
    }

    @Override
    default void salvar(Exemplar e) {
        gravar(new ExemplarEntity(e.id(), e.livroId(), e.tombo(), SituacaoExemplar.valueOf(e.situacao().name())));
    }
}
