package com.example.processo.persistencia;

import com.example.processo.CadastroFalso;
import com.example.processo.FilaEmMemoria;
import com.example.processo.SistemaDePdfFalso;
import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.Situacao;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.Valor;
import com.example.processo.persistencia.repositorio.ProcessoImobiliarioRepository;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Os repositories no Oracle: o que o negócio espera da gravação polimórfica, do compare-and-set e da chave de
 * idempotência. Cada teste grava processos próprios (número e matrícula aleatórios): o banco é compartilhado.
 */
@QuarkusTest
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class ProcessoRepositoryBancoTest {

    @Inject
    ProcessoRepository processos;

    @Inject
    ProcessoImobiliarioRepository imobiliarios;

    static String aleatorio() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
    }

    ProcessoImobiliario gravado(String matricula, String uf) {
        var processo = new ProcessoImobiliario("Ana Souza", new Cpf("52998224725"), new Valor("400000.00"), matricula,
                TipoImovel.RESIDENCIAL, new BigDecimal("500000.00"), uf);
        processo.abrir("TST-" + aleatorio(), LocalDate.of(2026, 10, 19));
        QuarkusTransaction.requiringNew().run(() -> processos.inserir(processo));
        return processo;
    }

    boolean mudar(ProcessoImobiliario processo, Situacao de, Situacao para, String motivo) {
        return QuarkusTransaction.requiringNew().call(() -> processos.mudarSituacao(processo.getId(), de, para, motivo));
    }

    @Test
    void buscarPeloPaiDevolveAFilhaCompleta() {
        var processo = gravado(aleatorio(), "SP");

        var lido = assertInstanceOf(ProcessoImobiliario.class, processos.buscar(processo.getId()).orElseThrow());

        assertEquals(ProcessoImobiliario.SEGMENTO, lido.getSegmento());
        assertEquals(processo.getNumero(), lido.getNumero());
        assertEquals("52998224725", lido.getProponenteCpf());
        assertEquals(Situacao.ABERTO, lido.getSituacao());
        assertEquals(processo.getMatricula(), lido.getMatricula());
        assertEquals(TipoImovel.RESIDENCIAL, lido.getTipoImovel());
        // NUMBER do Oracle não guarda zeros à direita: compare números, não texto
        assertEquals(0, new BigDecimal("400000.00").compareTo(lido.getValor()));
        assertEquals(0, new BigDecimal("500000.00").compareTo(lido.getValorAvaliacao()));
        assertEquals(Optional.empty(), processos.buscar(UUID.randomUUID()));
    }

    @Test
    void mudarSituacaoSoQuandoAindaEstaNaAnterior() {
        var processo = gravado(aleatorio(), "SP");

        assertTrue(mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null));
        assertFalse(mudar(processo, Situacao.ABERTO, Situacao.CANCELADO, null), "compare-and-set: já não está ABERTO");
        assertTrue(mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.RECUSADO, "Valor alto"));

        var lido = processos.buscar(processo.getId()).orElseThrow();
        assertEquals(Situacao.RECUSADO, lido.getSituacao());
        assertEquals("Valor alto", lido.getMotivoRecusa());
    }

    @Test
    void chaveDeIdempotenciaLigaAoProcessoUmaVezSo() {
        var chave = "msg-" + aleatorio();
        var processo = gravado(aleatorio(), "SP");
        var outro = gravado(aleatorio(), "SP");

        assertEquals(Optional.empty(), processos.processoDaChave(chave));

        QuarkusTransaction.requiringNew().run(() -> processos.registrarChave(chave, processo.getId()));

        assertEquals(Optional.of(processo.getId()), processos.processoDaChave(chave));
        assertThrows(RuntimeException.class, () -> QuarkusTransaction.requiringNew()
                .run(() -> processos.registrarChave(chave, outro.getId())));
        assertEquals(Optional.of(processo.getId()), processos.processoDaChave(chave));
    }

    @Test
    void emAndamentoSoAbertoOuEmProcessamentoNaMesmaMatriculaEUf() {
        var matricula = aleatorio();
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"));

        var processo = gravado(matricula, "SP");
        assertTrue(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "ABERTO");
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "RJ"), "outra UF");

        mudar(processo, Situacao.ABERTO, Situacao.EM_PROCESSAMENTO, null);
        assertTrue(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "EM_PROCESSAMENTO");

        mudar(processo, Situacao.EM_PROCESSAMENTO, Situacao.CONCLUIDO, null);
        assertFalse(imobiliarios.existeEmAndamentoParaMatricula(matricula, "SP"), "CONCLUIDO não está em andamento");
    }
}
