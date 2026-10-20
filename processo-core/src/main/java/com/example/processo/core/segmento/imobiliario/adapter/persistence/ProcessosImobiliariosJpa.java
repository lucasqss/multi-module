package com.example.processo.core.segmento.imobiliario.adapter.persistence;

import com.example.jpacore.ProcessoImobiliarioEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.jpacore.TipoImovel;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.estado.Situacao;
import com.example.processo.core.segmento.imobiliario.domain.DadosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.ProcessosImobiliarios;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessosImobiliariosJpa extends ProcessosImobiliarios {

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
    default void salvar(Processo<DadosImobiliarios> p) {
        var imovel = p.dados();
        inserir(new ProcessoImobiliarioEntity(p.id(), p.numero(), p.proponente().nome(), p.proponente().cpf(),
                p.valor(), SituacaoProcesso.valueOf(p.situacao().name()), p.dataAbertura(),
                imovel.matricula(), TipoImovel.valueOf(imovel.tipoImovel().name()), imovel.valorAvaliacao(),
                imovel.uf()));
    }

    @Override
    default Optional<Processo<DadosImobiliarios>> porId(UUID id) {
        return buscar(id).map(e -> new Processo<>(e.getId(), e.getNumero(), e.getSegmento(),
                new Proponente(e.getProponenteNome(), e.getProponenteCpf()), e.getValor(),
                Situacao.valueOf(e.getSituacao().name()), e.getMotivoRecusa(), e.getDataAbertura(),
                new DadosImobiliarios(e.getMatricula(), DadosImobiliarios.TipoImovel.valueOf(e.getTipoImovel().name()),
                        e.getValorAvaliacao(), e.getUf())));
    }
}
