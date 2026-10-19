package com.example.processo.imobiliario.adapter.persistence;

import com.example.jpacore.ProcessoImobiliarioEntity;
import com.example.jpacore.SituacaoProcesso;
import com.example.jpacore.TipoImovel;
import com.example.processo.imobiliario.domain.DadosImobiliarios;
import com.example.processo.imobiliario.domain.ProcessosImobiliarios;
import com.example.processo.motor.domain.Processo;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

@Repository
public interface ProcessosImobiliariosJpa extends ProcessosImobiliarios {

    /** situacao vem da entidade pai: a consulta na filha enxerga os campos herdados (join automático). */
    @Query("select count(*) from ProcessoImobiliarioEntity where matricula = :matricula and uf = :uf and situacao = :situacao")
    long contar(String matricula, String uf, SituacaoProcesso situacao);

    @Insert
    void inserir(ProcessoImobiliarioEntity processo);

    @Override
    default boolean existeAbertoParaMatricula(String matricula, String uf) {
        return contar(matricula, uf, SituacaoProcesso.ABERTO) > 0;
    }

    @Override
    default void salvar(Processo<DadosImobiliarios> p) {
        var imovel = p.dados();
        inserir(new ProcessoImobiliarioEntity(p.id(), p.numero(), p.proponente().nome(), p.proponente().cpf(),
                p.valor(), SituacaoProcesso.valueOf(p.situacao().name()), p.dataAbertura(),
                imovel.matricula(), TipoImovel.valueOf(imovel.tipoImovel().name()), imovel.valorAvaliacao(),
                imovel.uf()));
    }
}
