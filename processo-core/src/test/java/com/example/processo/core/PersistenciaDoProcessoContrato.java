package com.example.processo.core;

import com.example.processo.core.motor.application.port.out.Processos;
import com.example.processo.core.motor.application.port.out.Transacoes;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.segmento.imobiliario.application.port.out.ProcessosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.model.DadosImobiliarios.TipoImovel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrato dos ports de persistência: o que o motor e o segmento esperam de {@link Processos} e
 * {@link ProcessosImobiliarios}. Roda contra os fakes ({@code PersistenciaEmMemoriaTest}, aqui) e contra os
 * repositories no Oracle ({@code PersistenciaOracleTest}, no processo-app). Se um fake passar a se comportar
 * diferente do banco, um dos dois quebra; os testes do motor, que usam o fake, continuam valendo.
 *
 * <p>Cada teste grava processos próprios (número e matrícula aleatórios): no Oracle o banco é compartilhado.
 */
public abstract class PersistenciaDoProcessoContrato {

    protected abstract Processos processos();

    protected abstract ProcessosImobiliarios imobiliarios();

    protected abstract Transacoes transacoes();

    static String aleatorio() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
    }

    static Processo<DadosImobiliarios> novo(String matricula, String uf) {
        var imovel = new DadosImobiliarios(matricula, TipoImovel.RESIDENCIAL, new BigDecimal("500000.00"), uf);
        return Processo.abrir("TST-" + aleatorio(), "IMOBILIARIO",
                new Solicitacao<>(new Proponente("Ana Souza", "52998224725"), new Valor("400000.00"), imovel),
                LocalDate.of(2026, 10, 19));
    }

    Processo<DadosImobiliarios> gravado(String matricula, String uf) {
        var processo = novo(matricula, uf);
        transacoes().executar(() -> {
            imobiliarios().salvar(processo);
            return null;
        });
        return processo;
    }

    boolean mudar(Processo<?> processo, Situacao de, Situacao para, String motivo) {
        return transacoes().executar(() -> processos().mudarSituacao(processo.id(), de, para, motivo));
    }

    @Test
    void idaEVoltaDevolveOMesmoProcesso() {
        var processo = gravado(aleatorio(), "SP");

        assertEquals(Optional.of(processo), imobiliarios().porId(processo.id()));
        assertEquals(Optional.of("IMOBILIARIO"), processos().segmentoDe(processo.id()));
    }

    @Test
    void processoInexistente() {
        var inexistente = UUID.randomUUID();

        assertEquals(Optional.empty(), processos().segmentoDe(inexistente));
        assertEquals(Optional.empty(), imobiliarios().porId(inexistente));
    }

    @Test
    void mudarSituacaoSoQuandoAindaEstaNaAnterior() {
        var processo = gravado(aleatorio(), "SP");

        assertTrue(mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null));
        assertFalse(mudar(processo, Situacao.ABERTO, Situacao.CANCELADO, null), "compare-and-set: já não está ABERTO");

        assertEquals(Situacao.EM_PROCESSAMENTO, imobiliarios().porId(processo.id()).orElseThrow().situacao());
    }

    @Test
    void recusaGravaOMotivo() {
        var processo = gravado(aleatorio(), "SP");
        mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null);

        assertTrue(mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.RECUSADO, "Valor alto"));

        var recusado = imobiliarios().porId(processo.id()).orElseThrow();
        assertEquals(Situacao.RECUSADO, recusado.situacao());
        assertEquals("Valor alto", recusado.motivoRecusa());
    }

    @Test
    void emAndamentoSoAbertoOuEmProcessamentoNaMesmaMatriculaEUf() {
        var matricula = aleatorio();
        assertFalse(imobiliarios().existeEmAndamentoParaMatricula(matricula, "SP"));

        var processo = gravado(matricula, "SP");
        assertTrue(imobiliarios().existeEmAndamentoParaMatricula(matricula, "SP"), "ABERTO");
        assertFalse(imobiliarios().existeEmAndamentoParaMatricula(matricula, "RJ"), "outra UF");

        mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null);
        assertTrue(imobiliarios().existeEmAndamentoParaMatricula(matricula, "SP"), "EM_PROCESSAMENTO");

        mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.CONCLUIDO, null);
        assertFalse(imobiliarios().existeEmAndamentoParaMatricula(matricula, "SP"), "CONCLUIDO não está em andamento");
    }
}
