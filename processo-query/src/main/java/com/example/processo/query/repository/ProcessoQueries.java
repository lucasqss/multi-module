package com.example.processo.query.repository;

import com.example.jpacore.SituacaoProcesso;
import com.example.processo.query.dto.DetalheImobiliario;
import com.example.processo.query.dto.DetalheVeicular;
import com.example.processo.query.dto.ResumoProcesso;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessoQueries {

    String RESUMO = """
            select new com.example.processo.query.dto.ResumoProcesso(
                p.id, p.numero, p.segmento, p.proponenteNome, p.valor, p.situacao, p.dataAbertura)
            from ProcessoEntity p""";

    /**
     * Listagem polimórfica e paginada: consulta o pai (todos os segmentos) projetando só colunas de PROCESSO, então
     * segmento novo não acrescenta join aqui. O Hibernate gera também o count para o total.
     */
    @Query(RESUMO + " order by p.numero")
    Page<ResumoProcesso> todos(PageRequest pagina);

    @Query(RESUMO + " where p.situacao = :situacao order by p.numero")
    Page<ResumoProcesso> naSituacao(SituacaoProcesso situacao, PageRequest pagina);

    /** Detalhe de um segmento: consulta direto a filha, que já enxerga os campos herdados do pai. */
    @Query("""
            select new com.example.processo.query.dto.DetalheImobiliario(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.matricula, p.tipoImovel, p.valorAvaliacao, p.uf)
            from ProcessoImobiliarioEntity p
            where p.id = :id""")
    Optional<DetalheImobiliario> imobiliario(UUID id);

    @Query("""
            select new com.example.processo.query.dto.DetalheVeicular(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.placa, p.anoFabricacao, p.valorFipe)
            from ProcessoVeicularEntity p
            where p.id = :id""")
    Optional<DetalheVeicular> veicular(UUID id);
}
