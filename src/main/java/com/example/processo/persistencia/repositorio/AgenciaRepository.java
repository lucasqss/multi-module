package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.Agencia;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenciaRepository {

    @Find
    Optional<Agencia> buscar(String codigo);

    @Query("from Agencia order by codigo")
    List<Agencia> todas();

    @Insert
    void inserir(Agencia agencia);

    @Update
    void atualizar(Agencia agencia);

    @Delete
    void excluir(String codigo);
}
