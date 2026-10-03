package com.example.biblioteca.command.adapter.persistence;

import com.example.biblioteca.command.domain.Emprestimo;
import com.example.biblioteca.command.domain.Emprestimos;
import com.example.jpacore.EmprestimoEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmprestimosJpa extends Emprestimos {

    @Find
    Optional<EmprestimoEntity> buscar(UUID id);

    @Query("from EmprestimoEntity where leitorId = :leitorId and dataDevolucao is null")
    List<EmprestimoEntity> buscarAbertos(UUID leitorId);

    @Save
    void gravar(EmprestimoEntity emprestimo);

    @Override
    default Optional<Emprestimo> porId(UUID id) {
        return buscar(id).map(EmprestimosJpa::paraDominio);
    }

    @Override
    default List<Emprestimo> abertosDo(UUID leitorId) {
        return buscarAbertos(leitorId).stream().map(EmprestimosJpa::paraDominio).toList();
    }

    @Override
    default void salvar(Emprestimo e) {
        gravar(new EmprestimoEntity(e.id(), e.leitorId(), e.exemplarId(), e.data(), e.dataPrevista(),
                e.renovacoes(), e.dataDevolucao()));
    }

    private static Emprestimo paraDominio(EmprestimoEntity e) {
        return new Emprestimo(e.getId(), e.getLeitorId(), e.getExemplarId(), e.getData(), e.getDataPrevista(),
                e.getRenovacoes(), e.getDataDevolucao());
    }
}
