package com.example.processo.negocio.processo;

import com.example.processo.CadastroFalso;
import com.example.processo.FilaEmMemoria;
import com.example.processo.SistemaDePdfFalso;
import com.example.processo.modelo.Cpf;
import com.example.processo.modelo.ProcessoImobiliario;
import com.example.processo.modelo.ProcessoVeicular;
import com.example.processo.modelo.RegraVioladaException;
import com.example.processo.modelo.SistemaExternoIndisponivelException;
import com.example.processo.modelo.Situacao;
import com.example.processo.modelo.TipoImovel;
import com.example.processo.modelo.TransicaoInvalidaException;
import com.example.processo.modelo.Valor;
import com.example.processo.persistencia.repositorio.ProcessoRepository;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O motor com o banco de verdade (Oracle do Dev Services) e os sistemas externos falsos. Cada teste usa matrículas e
 * chaves próprias: o banco é compartilhado com os outros testes.
 */
@QuarkusTest
// O mesmo conjunto de recursos dos outros @QuarkusTest: o app (e o Oracle) sobe uma vez só
@WithTestResource(FilaEmMemoria.class)
@WithTestResource(CadastroFalso.class)
@WithTestResource(SistemaDePdfFalso.class)
class ProcessoServiceBancoTest {

    static final String ANO = Integer.toString(LocalDate.now().getYear());

    @Inject
    ProcessoService motor;

    @Inject
    ProcessoRepository repositorio;

    static String matricula() {
        return Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000L, 2_000_000_000L));
    }

    static ProcessoImobiliario imobiliario(String nome, String cpf, String matricula) {
        return new ProcessoImobiliario(nome, new Cpf(cpf), new Valor("400000.00"), matricula, TipoImovel.RESIDENCIAL,
                new BigDecimal("500000.00"), "SP");
    }

    static ProcessoImobiliario imobiliario(String matricula) {
        return imobiliario("Ana Souza", "52998224725", matricula);
    }

    static ProcessoVeicular veicular(String cpf, String placa, int anoFabricacao) {
        return new ProcessoVeicular("Ana Souza", new Cpf(cpf), new Valor("45000.00"), placa, anoFabricacao,
                new BigDecimal("50000.00"));
    }

    static List<String> pedidosDePdf(String numero) {
        return SistemaDePdfFalso.PEDIDOS.stream().filter(p -> p.contains("\"referencia\":\"" + numero + "\"")).toList();
    }

    @Test
    void criaComONumeroDaModalidadeGravaEPedeOPdf() {
        var imovel = motor.criar(imobiliario(matricula()));
        var veiculo = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 2));

        assertTrue(imovel.getNumero().matches("IMO-" + ANO + "-\\d{6}"), imovel.getNumero());
        assertTrue(veiculo.getNumero().matches("VEI-" + ANO + "-\\d{6}"), veiculo.getNumero());
        var gravado = assertInstanceOf(ProcessoImobiliario.class, repositorio.buscar(imovel.getId()).orElseThrow());
        assertEquals(Situacao.ABERTO, gravado.getSituacao());
        assertEquals(imovel.getMatricula(), gravado.getMatricula());
        assertEquals(1, pedidosDePdf(imovel.getNumero()).size());
        assertEquals(1, pedidosDePdf(veiculo.getNumero()).size());
        // Só os atributos da modalidade: nada do que o enhancement do Hibernate acrescenta à entidade no build
        Map<String, Object> campos = new JsonObject(pedidosDePdf(imovel.getNumero()).getFirst()).getJsonObject("campos").getMap();
        assertEquals(Set.of("matricula", "tipoImovel", "valorAvaliacao", "uf"), campos.keySet());
    }

    @Test
    void mesmaChaveDevolveOProcessoJaCriadoSemPassarPelasRegras() {
        var chave = "chave-" + UUID.randomUUID();
        var matricula = matricula();

        var primeiro = motor.criar(imobiliario(matricula), chave);
        // A mesma matrícula seria recusada pela regra de processo em andamento: a chave devolve antes das regras
        var reentregue = motor.criar(imobiliario(matricula), chave);

        assertEquals(primeiro.getId(), reentregue.getId());
        assertEquals(primeiro.getNumero(), reentregue.getNumero());
        assertEquals(1, pedidosDePdf(primeiro.getNumero()).size(), "não pede outro PDF");
    }

    @Test
    void recusaNaoRegistraAChave() {
        var chave = "chave-" + UUID.randomUUID();

        assertThrows(RegraVioladaException.class, () -> motor.criar(imobiliario(" ", "52998224725", matricula()), chave));

        assertEquals(Optional.empty(), repositorio.processoDaChave(chave), "a mesma mensagem corrigida ainda pode criar");
    }

    @Test
    void regrasComunsVemAntesDasDaModalidade() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(imobiliario(" ", "52998224725", "matricula-invalida")));

        assertEquals("Nome do proponente é obrigatório", erro.getMessage());
    }

    @Test
    void sistemaDeCadastroSoEhConsultadoSeAsOutrasRegrasPassarem() {
        // Com a placa inválida, a regra recusa antes de chamar o cadastro (que, para este CPF, está fora do ar)
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_SISTEMA_FORA, "placa", 2022)));
        assertEquals("Placa inválida", erro.getMessage());

        assertThrows(SistemaExternoIndisponivelException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_SISTEMA_FORA, "ABC1D23", 2022)));
    }

    @Test
    void situacaoCadastralIrregularRecusa() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(veicular(CadastroFalso.CPF_IRREGULAR, "ABC1D23", 2022)));

        assertEquals("Situação cadastral irregular para o CPF " + CadastroFalso.CPF_IRREGULAR, erro.getMessage());
    }

    @Test
    void falhaNoPdfNaoDesfazACriacao() {
        var matricula = SistemaDePdfFalso.FALHA + ThreadLocalRandom.current().nextInt(1_000_000, 10_000_000);

        var processo = motor.criar(imobiliario(matricula));

        assertEquals(1, pedidosDePdf(processo.getNumero()).size());
        assertEquals(Situacao.ABERTO, repositorio.buscar(processo.getId()).orElseThrow().getSituacao());
    }

    @Test
    void cicloDeVidaGravaCadaSituacao() {
        var aprovado = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 2));
        var recusado = motor.criar(veicular("52998224725", "ABC1D23", LocalDate.now().getYear() - 11));
        var cancelado = motor.criar(veicular("52998224725", "ABC1D23", 2022));

        assertEquals(Situacao.CONCLUIDO, motor.processar(aprovado.getId()).getSituacao());
        var processado = motor.processar(recusado.getId());
        motor.cancelar(cancelado.getId());

        assertEquals(Situacao.CONCLUIDO, repositorio.buscar(aprovado.getId()).orElseThrow().getSituacao());
        var gravadoRecusado = repositorio.buscar(recusado.getId()).orElseThrow();
        assertEquals(Situacao.RECUSADO, gravadoRecusado.getSituacao());
        assertEquals(processado.getMotivoRecusa(), gravadoRecusado.getMotivoRecusa());
        assertEquals(Situacao.CANCELADO, repositorio.buscar(cancelado.getId()).orElseThrow().getSituacao());
    }

    @Test
    void transicaoInvalidaNaoGrava() {
        var processo = motor.criar(veicular("52998224725", "ABC1D23", 2022));
        motor.cancelar(processo.getId());

        var erro = assertThrows(TransicaoInvalidaException.class, () -> motor.processar(processo.getId()));

        assertEquals("Processo CANCELADO não pode ser processado", erro.getMessage());
        assertEquals(Situacao.CANCELADO, repositorio.buscar(processo.getId()).orElseThrow().getSituacao());
    }

    @Test
    void processoInexistente() {
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.processar(UUID.randomUUID()));
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.cancelar(UUID.randomUUID()));
    }
}
