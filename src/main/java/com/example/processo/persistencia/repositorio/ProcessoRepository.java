package com.example.processo.persistencia.repositorio;

import com.example.processo.modelo.ChaveIdempotencia;
import com.example.processo.modelo.Processo;
import com.example.processo.modelo.Situacao;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import org.hibernate.annotations.processing.SQL;

import java.util.Optional;
import java.util.UUID;

/**
 * Gravação e leitura de processos de qualquer modalidade. A implementação é gerada pelo Hibernate Processor na
 * compilação ({@code ProcessoRepository_}). Como {@link Processo} é a raiz da herança, gravar uma filha insere nas duas
 * tabelas e ler pelo id devolve a filha certa ({@code ProcessoImobiliario}, {@code ProcessoVeicular}...).
 */
@Repository
public interface ProcessoRepository {

    @Insert
    void inserir(Processo processo);

    @Find
    Optional<Processo> buscar(UUID id);

    /** Sequence do Oracle: única para todas as modalidades e sem corrida entre transações. */
    @SQL("select SEQ_NUMERO_PROCESSO.nextval from dual")
    long proximoNumero();

    /**
     * SQL nativo de propósito: a situação está só na tabela pai, e um update HQL numa hierarquia JOINED faz o
     * Hibernate usar a estratégia multi-tabela (tabelas temporárias no Oracle).
     */
    @SQL("update PROCESSO set SITUACAO = :para, MOTIVO_RECUSA = :motivo where ID = :id and SITUACAO = :de")
    int atualizarSituacao(UUID id, String de, String para, String motivo);

    /**
     * Compare-and-set: troca a situação só se ela ainda for {@code de}. {@code false} quer dizer que outra requisição
     * mudou o processo antes.
     */
    default boolean mudarSituacao(UUID id, Situacao de, Situacao para, String motivoRecusa) {
        return atualizarSituacao(id, de.name(), para.name(), motivoRecusa) == 1;
    }

    @Find
    Optional<ChaveIdempotencia> buscarChave(String chave);

    /** Falha se a chave já existir (PK): duas criações simultâneas com a mesma chave não passam as duas. */
    @Insert
    void inserirChave(ChaveIdempotencia chave);
}
