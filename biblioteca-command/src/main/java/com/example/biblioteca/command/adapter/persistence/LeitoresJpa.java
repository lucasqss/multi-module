package com.example.biblioteca.command.adapter.persistence;

import com.example.biblioteca.command.domain.Leitor;
import com.example.biblioteca.command.domain.Leitores;
import com.example.jpacore.LeitorEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeitoresJpa extends Leitores {

    @Find
    Optional<LeitorEntity> buscar(UUID id);

    @Query("select count(*) from LeitorEntity where email = :email")
    long contarPorEmail(String email);

    @Save
    void gravar(LeitorEntity leitor);

    @Override
    default Optional<Leitor> porId(UUID id) {
        return buscar(id).map(l -> new Leitor(l.getId(), l.getNome(), l.getEmail(), l.getSuspensoAte()));
    }

    @Override
    default boolean existeEmail(String email) {
        return contarPorEmail(email) > 0;
    }

    @Override
    default void salvar(Leitor l) {
        gravar(new LeitorEntity(l.id(), l.nome(), l.email(), l.suspensoAte()));
    }
}
