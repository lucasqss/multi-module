package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.Situacao;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

/** Consultas que só a modalidade imobiliária usa. Modalidade sem consulta própria não precisa de repository. */
@Repository
public interface ProcessoImobiliarioRepository {

    List<Situacao> EM_ANDAMENTO = List.of(Situacao.ABERTO, Situacao.EM_PROCESSAMENTO);

    /** situacao vem da entidade pai: a consulta na filha enxerga os campos herdados (join automático). */
    @Query("select count(*) from ProcessoImobiliario where matricula = :matricula and uf = :uf and situacao in :situacoes")
    long contar(String matricula, String uf, List<Situacao> situacoes);

    /** Em andamento = aberto ou em processamento. */
    default boolean existeEmAndamentoParaMatricula(String matricula, String uf) {
        return contar(matricula, uf, EM_ANDAMENTO) > 0;
    }
}
