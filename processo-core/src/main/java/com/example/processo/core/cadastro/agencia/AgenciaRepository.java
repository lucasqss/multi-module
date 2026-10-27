package com.example.processo.core.cadastro.agencia;

import com.example.jpacore.AgenciaEntity;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;

import java.util.List;
import java.util.Optional;

/** Sem port: num cadastro simples o serviço usa o repository direto. */
@Repository
public interface AgenciaRepository {

    @Find
    Optional<AgenciaEntity> buscar(String codigo);

    @Query("from AgenciaEntity order by codigo")
    List<AgenciaEntity> todas();

    @Insert
    void inserir(AgenciaEntity agencia);

    @Update
    void atualizar(AgenciaEntity agencia);

    @Delete
    void excluir(String codigo);
}
