package com.example.processo.persistencia.consulta;

import com.example.processo.modelo.Situacao;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Consultas somente leitura para as telas: projetam direto das tabelas para o formato da resposta, sem carregar a
 * entidade nem passar pelas regras. A apresentação usa este repository direto, sem um serviço que só repassaria.
 */
@Repository
public interface ProcessoConsultas {

    String RESUMO = """
            select new com.example.processo.persistencia.consulta.ResumoProcesso(
                p.id, p.numero, p.segmento, p.proponenteNome, p.valor, p.situacao, p.dataAbertura)
            from Processo p""";

    /**
     * Listagem polimórfica e paginada: consulta o pai (todas as modalidades) projetando só colunas de PROCESSO, então
     * modalidade nova não acrescenta join aqui. O Hibernate gera também o count para o total.
     */
    @Query(RESUMO + " order by p.numero")
    Page<ResumoProcesso> todos(PageRequest pagina);

    @Query(RESUMO + " where p.situacao = :situacao order by p.numero")
    Page<ResumoProcesso> naSituacao(Situacao situacao, PageRequest pagina);

    /** Detalhe de uma modalidade: consulta direto a filha, que já enxerga os campos herdados do pai. */
    @Query("""
            select new com.example.processo.persistencia.consulta.DetalheImobiliario(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.matricula, p.tipoImovel, p.valorAvaliacao, p.uf)
            from ProcessoImobiliario p
            where p.id = :id""")
    Optional<DetalheImobiliario> imobiliario(UUID id);

    @Query("""
            select new com.example.processo.persistencia.consulta.DetalheVeicular(
                p.id, p.numero, p.proponenteNome, p.proponenteCpf, p.valor, p.situacao, p.motivoRecusa, p.dataAbertura,
                p.placa, p.anoFabricacao, p.valorFipe)
            from ProcessoVeicular p
            where p.id = :id""")
    Optional<DetalheVeicular> veicular(UUID id);
}
