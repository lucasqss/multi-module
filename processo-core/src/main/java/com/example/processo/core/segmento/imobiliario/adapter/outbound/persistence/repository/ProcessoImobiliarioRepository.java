package com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.repository;

import com.example.jpacore.ProcessoImobiliarioEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.segmento.imobiliario.adapter.outbound.persistence.mapper.ProcessoImobiliarioMapper;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementa o port do segmento: os métodos Jakarta Data trabalham com a entidade e os {@code default} convertem
 * pelo mapper.
 */
@Repository
public interface ProcessoImobiliarioRepository extends ProcessosImobiliarios {

    List<SituacaoProcesso> EM_ANDAMENTO = List.of(SituacaoProcesso.ABERTO, SituacaoProcesso.EM_PROCESSAMENTO);

    /** situacao vem da entidade pai: a consulta na filha enxerga os campos herdados (join automático). */
    @Query("select count(*) from ProcessoImobiliarioEntity where matricula = :matricula and uf = :uf and situacao in :situacoes")
    long contar(String matricula, String uf, List<SituacaoProcesso> situacoes);

    @Insert
    void inserir(ProcessoImobiliarioEntity processo);

    @Find
    Optional<ProcessoImobiliarioEntity> buscar(UUID id);

    @Override
    default boolean existeEmAndamentoParaMatricula(String matricula, String uf) {
        return contar(matricula, uf, EM_ANDAMENTO) > 0;
    }

    @Override
    default void salvar(Processo<DadosImobiliarios> processo) {
        inserir(ProcessoImobiliarioMapper.paraEntidade(processo));
    }

    @Override
    default Optional<Processo<DadosImobiliarios>> porId(UUID id) {
        return buscar(id).map(ProcessoImobiliarioMapper::paraDominio);
    }
}
