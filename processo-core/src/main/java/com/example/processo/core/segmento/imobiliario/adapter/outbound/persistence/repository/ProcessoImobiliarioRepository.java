package com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.repository;

import com.example.jpacore.ProcessoImobiliarioEntity;
import com.example.jpacore.SituacaoProcesso;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Jakarta Data puro, sobre a entidade filha: não conhece o domínio (quem converte é o mapper). */
@Repository
public interface ProcessoImobiliarioRepository {

    /** situacao vem da entidade pai: a consulta na filha enxerga os campos herdados (join automático). */
    @Query("select count(*) from ProcessoImobiliarioEntity where matricula = :matricula and uf = :uf and situacao in :situacoes")
    long contar(String matricula, String uf, List<SituacaoProcesso> situacoes);

    @Insert
    void inserir(ProcessoImobiliarioEntity processo);

    @Find
    Optional<ProcessoImobiliarioEntity> buscar(UUID id);
}
