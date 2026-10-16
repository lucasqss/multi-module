package com.example.biblioteca.query;

import com.example.jpacore.EmprestimoEntity;
import com.example.jpacore.LeitorEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CirculacaoQueries {

    @Find
    Optional<LeitorEntity> leitor(UUID id);

    @Query("""
            select e from EmprestimoEntity e
            join fetch e.exemplar x
            join fetch x.livro
            where e.leitorId = :leitorId and e.dataDevolucao is null
            order by e.dataPrevista""")
    List<EmprestimoEntity> abertosDo(UUID leitorId);

    @Query("""
            select e from EmprestimoEntity e
            join fetch e.leitor
            join fetch e.exemplar x
            join fetch x.livro
            where e.dataDevolucao is null and e.dataPrevista < :hoje
            order by e.dataPrevista""")
    List<EmprestimoEntity> atrasadosEm(LocalDate hoje);
}
