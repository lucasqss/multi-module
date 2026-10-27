package com.example.processo.core.motor.application.service;

import com.example.processo.core.Fakes;
import com.example.processo.core.motor.application.port.out.SituacaoCadastral;
import com.example.processo.core.motor.application.port.spi.Segmento;
import com.example.processo.core.motor.domain.exception.ConflitoDeSituacaoException;
import com.example.processo.core.motor.domain.exception.ProcessoNaoEncontradoException;
import com.example.processo.core.motor.domain.exception.RegraVioladaException;
import com.example.processo.core.motor.domain.model.Avaliacao;
import com.example.processo.core.motor.domain.model.Cpf;
import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Processo;
import com.example.processo.core.motor.domain.model.Proponente;
import com.example.processo.core.motor.domain.model.Solicitacao;
import com.example.processo.core.motor.domain.model.Valor;
import com.example.processo.core.motor.domain.rule.Especificacao;
import com.example.processo.core.motor.domain.state.Situacao;
import com.example.processo.core.motor.domain.state.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O motor com dois segmentos de mentira: o mesmo fluxo e o mesmo ciclo de vida atendem os dois. */
class MotorDeProcessamentoTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Proponente ANA = new Proponente("Ana Souza", "52998224725");

    record DadosAuto(String placa) implements DadosDoSegmento {
    }

    record DadosServico(String descricao) implements DadosDoSegmento {
    }

    /** Segmento de teste: grava e lê no banco em memória; a decisão do processamento é passada no construtor. */
    class SegmentoEmMemoria<D extends DadosDoSegmento> implements Segmento<D> {
        final String codigo;
        final String prefixo;
        final Class<D> tipo;
        final Especificacao<Solicitacao<D>> regras;
        final Function<Processo<D>, Avaliacao> avaliacao;
        final List<Processo<D>> gravados = new ArrayList<>();

        SegmentoEmMemoria(String codigo, String prefixo, Class<D> tipo, Especificacao<Solicitacao<D>> regras,
                          Function<Processo<D>, Avaliacao> avaliacao) {
            this.codigo = codigo;
            this.prefixo = prefixo;
            this.tipo = tipo;
            this.regras = regras;
            this.avaliacao = avaliacao;
        }

        public String codigo() { return codigo; }
        public String prefixo() { return prefixo; }
        public Class<D> tipoDosDados() { return tipo; }
        public Especificacao<Solicitacao<D>> regras() { return regras; }
        public void salvar(Processo<D> processo) { gravados.add(processo); banco.processos.put(processo.id(), processo); }
        @SuppressWarnings("unchecked")
        public Optional<Processo<D>> carregar(UUID id) { return Optional.ofNullable((Processo<D>) banco.processos.get(id)); }
        public Avaliacao avaliar(Processo<D> processo) { return avaliacao.apply(processo); }
    }

    final Fakes.BancoEmMemoria banco = new Fakes.BancoEmMemoria();
    /** Auto: recusa no processamento placas antigas (sem letra na 5ª posição). */
    final SegmentoEmMemoria<DadosAuto> auto = new SegmentoEmMemoria<>("AUTO", "AUT", DadosAuto.class,
            Especificacao.exige(s -> s.dados().placa().matches("[A-Z]{3}\\d[A-Z0-9]\\d{2}"), "Placa inválida"),
            p -> p.dados().placa().matches("[A-Z]{3}\\d{4}") ? Avaliacao.recusado("Placa antiga") : Avaliacao.aprovado());
    final SegmentoEmMemoria<DadosServico> servico = new SegmentoEmMemoria<>("SERVICO", "SRV", DadosServico.class,
            s -> Optional.empty(), p -> Avaliacao.aprovado());
    final AtomicLong sequencia = new AtomicLong();
    final Fakes.DocumentosEmMemoria documentos = new Fakes.DocumentosEmMemoria();
    /** CPF que o sistema externo falso considera irregular. */
    static final String CPF_IRREGULAR = "11144477735";
    final List<Cpf> consultados = new ArrayList<>();
    final SituacaoCadastral situacaoCadastral = cpf -> consultados.add(cpf) && !cpf.numero().equals(CPF_IRREGULAR);
    final MotorDeProcessamento motor = new MotorDeProcessamento(List.of(auto, servico), situacaoCadastral,
            sequencia::incrementAndGet, banco, Fakes.semTransacao(),
            Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC), documentos);

    Processo<?> criarAuto(String placa) {
        return motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto(placa)));
    }

    Situacao situacaoNoBanco(Processo<?> processo) {
        return banco.processos.get(processo.id()).situacao();
    }

    @Test
    void mesmoMotorAtendeTodosOsSegmentosComNumeracaoUnica() {
        var carro = motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("ABC1D23")));
        var reforma = motor.criar(new Solicitacao<>(ANA, new Valor("8000"), new DadosServico("Reforma")));

        assertEquals("AUT-2026-000001", carro.numero());
        assertEquals("SRV-2026-000002", reforma.numero());
        assertEquals(List.of(carro), auto.gravados);
        assertEquals(List.of(reforma), servico.gravados);
        assertEquals(List.of(carro, reforma), documentos.pedidos);
    }

    @Test
    void falhaNoPdfNaoDesfazACriacao() {
        documentos.simularFalha = true;

        var processo = criarAuto("ABC1D23");

        assertEquals(List.of(processo), auto.gravados);
        assertEquals(List.of(processo), documentos.pedidos);
    }

    @Test
    void mesmaChaveDevolveOProcessoJaCriado() {
        var solicitacao = new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("ABC1D23"));

        var primeiro = motor.criar(solicitacao, "msg-1");
        var reentregue = motor.criar(solicitacao, "msg-1");

        assertEquals(primeiro, reentregue);
        assertEquals(1, sequencia.get(), "não gera outro número");
        assertEquals(List.of(primeiro), auto.gravados);
        assertEquals(List.of(primeiro), documentos.pedidos, "não pede outro PDF");
        assertEquals(List.of(ANA.cpf()), consultados, "não consulta o cadastro de novo");
    }

    @Test
    void chavesDiferentesCriamProcessosDiferentes() {
        var solicitacao = new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("ABC1D23"));

        var primeiro = motor.criar(solicitacao, "msg-1");
        var segundo = motor.criar(solicitacao, "msg-2");

        assertEquals(List.of(primeiro, segundo), auto.gravados);
        assertEquals(primeiro.id(), banco.chaves.get("msg-1"));
        assertEquals(segundo.id(), banco.chaves.get("msg-2"));
    }

    @Test
    void recusaNaoRegistraAChave() {
        assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("placa")), "msg-1"));

        assertTrue(banco.chaves.isEmpty(), "a mesma mensagem corrigida ainda pode criar o processo");
    }

    @Test
    void processoNasceAbertoComOsDadosDaSolicitacao() {
        var processo = motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("ABC1D23")));

        assertEquals("AUTO", processo.segmento());
        assertEquals(Situacao.ABERTO, processo.situacao());
        assertEquals(HOJE, processo.dataAbertura());
        assertEquals(ANA, processo.proponente());
        assertEquals(new DadosAuto("ABC1D23"), processo.dados());
    }

    @Test
    void regraDoSegmentoVioladaNaoGravaNemPedeDocumento() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("placa"))));

        assertEquals("Placa inválida", erro.getMessage());
        assertTrue(auto.gravados.isEmpty());
        assertTrue(documentos.pedidos.isEmpty());
        assertEquals(0, sequencia.get());
    }

    @Test
    void situacaoCadastralIrregularRecusaSemGerarNumero() {
        var irregular = new Proponente("Bruno Lima", CPF_IRREGULAR);
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(irregular, new Valor("50000"), new DadosAuto("ABC1D23"))));

        assertEquals("Situação cadastral irregular para o CPF " + CPF_IRREGULAR, erro.getMessage());
        assertTrue(auto.gravados.isEmpty());
        assertTrue(documentos.pedidos.isEmpty());
        assertEquals(0, sequencia.get());
    }

    @Test
    void sistemaExternoSoEhConsultadoSeAsOutrasRegrasPassarem() {
        assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("placa"))));
        assertTrue(consultados.isEmpty());

        motor.criar(new Solicitacao<>(ANA, new Valor("50000"), new DadosAuto("ABC1D23")));
        assertEquals(List.of(ANA.cpf()), consultados);
    }

    @Test
    void regrasComunsVemAntesDasDoSegmento() {
        var semNome = new Proponente("", "52998224725");
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(semNome, new Valor("50000"), new DadosAuto("placa"))));

        assertEquals("Nome do proponente é obrigatório", erro.getMessage());
    }

    @Test
    void dadosSemSegmentoRegistrado() {
        record DadosRural(String car) implements DadosDoSegmento {
        }
        assertThrows(IllegalArgumentException.class,
                () -> motor.criar(new Solicitacao<>(ANA, new Valor("10"), new DadosRural("x"))));
    }

    @Test
    void segmentoRegistradoDuasVezes() {
        var outroAuto = new SegmentoEmMemoria<>("AUTO", "AUT", DadosServico.class, s -> Optional.empty(),
                p -> Avaliacao.aprovado());
        assertThrows(IllegalStateException.class, () -> new MotorDeProcessamento(List.of(auto, outroAuto),
                situacaoCadastral, sequencia::incrementAndGet, banco, Fakes.semTransacao(), Clock.systemUTC(),
                documentos));
    }

    // Ciclo de vida: o mesmo para todos os segmentos; cada um só decide aprovar ou recusar

    @Test
    void processarAprovadoTerminaConcluido() {
        var processo = criarAuto("ABC1D23");

        var processado = motor.processar(processo.id());

        assertEquals(Situacao.CONCLUIDO, processado.situacao());
        assertEquals(Situacao.CONCLUIDO, situacaoNoBanco(processo));
    }

    @Test
    void processarRecusadoGuardaOMotivoDoSegmento() {
        var processo = criarAuto("ABC1234");

        var processado = motor.processar(processo.id());

        assertEquals(Situacao.RECUSADO, processado.situacao());
        assertEquals("Placa antiga", processado.motivoRecusa());
        assertEquals("Placa antiga", banco.processos.get(processo.id()).motivoRecusa());
    }

    @Test
    void mesmoCicloParaOutroSegmento() {
        var reforma = motor.criar(new Solicitacao<>(ANA, new Valor("8000"), new DadosServico("Reforma")));

        assertEquals(Situacao.CONCLUIDO, motor.processar(reforma.id()).situacao());
    }

    @Test
    void transicaoInvalidaNaoGrava() {
        var processo = criarAuto("ABC1D23");
        motor.processar(processo.id());

        var erro = assertThrows(TransicaoInvalidaException.class, () -> motor.processar(processo.id()));
        assertEquals("Processo CONCLUIDO não pode ser processado", erro.getMessage());
        assertThrows(TransicaoInvalidaException.class, () -> motor.cancelar(processo.id()));
        assertEquals(Situacao.CONCLUIDO, situacaoNoBanco(processo));
    }

    @Test
    void cancelarSoQuandoAberto() {
        var processo = criarAuto("ABC1D23");

        assertEquals(Situacao.CANCELADO, motor.cancelar(processo.id()).situacao());
        assertEquals(Situacao.CANCELADO, situacaoNoBanco(processo));
        assertThrows(TransicaoInvalidaException.class, () -> motor.processar(processo.id()));
    }

    @Test
    void outraRequisicaoMudouOProcessoNoMeio() {
        var processo = criarAuto("ABC1D23");
        banco.simularConflito = true;

        assertThrows(ConflitoDeSituacaoException.class, () -> motor.processar(processo.id()));
        assertEquals(Situacao.ABERTO, situacaoNoBanco(processo));
    }

    @Test
    void processoInexistente() {
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.processar(UUID.randomUUID()));
        assertThrows(ProcessoNaoEncontradoException.class, () -> motor.cancelar(UUID.randomUUID()));
    }
}
