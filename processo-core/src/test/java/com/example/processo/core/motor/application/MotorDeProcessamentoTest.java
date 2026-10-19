package com.example.processo.core.motor.application;

import com.example.processo.core.motor.domain.DadosDoSegmento;
import com.example.processo.core.motor.domain.Especificacao;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.ProcessoCriado;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.RegraVioladaException;
import com.example.processo.core.motor.domain.Segmento;
import com.example.processo.core.motor.domain.Solicitacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O motor com dois segmentos de mentira: o mesmo fluxo atende os dois, cada um com as próprias regras. */
class MotorDeProcessamentoTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Proponente ANA = new Proponente("Ana Souza", "52998224725");

    record DadosAuto(String placa) implements DadosDoSegmento {
    }

    record DadosServico(String descricao) implements DadosDoSegmento {
    }

    /** Segmento de teste: guarda o que o motor mandou gravar. */
    static class SegmentoEmMemoria<D extends DadosDoSegmento> implements Segmento<D> {
        final String codigo;
        final String prefixo;
        final Class<D> tipo;
        final Especificacao<Solicitacao<D>> regras;
        final List<Processo<D>> gravados = new ArrayList<>();

        SegmentoEmMemoria(String codigo, String prefixo, Class<D> tipo, Especificacao<Solicitacao<D>> regras) {
            this.codigo = codigo;
            this.prefixo = prefixo;
            this.tipo = tipo;
            this.regras = regras;
        }

        public String codigo() { return codigo; }
        public String prefixo() { return prefixo; }
        public Class<D> tipoDosDados() { return tipo; }
        public Especificacao<Solicitacao<D>> regras() { return regras; }
        public void salvar(Processo<D> processo) { gravados.add(processo); }
    }

    final SegmentoEmMemoria<DadosAuto> auto = new SegmentoEmMemoria<>("AUTO", "AUT", DadosAuto.class,
            Especificacao.exige(s -> s.dados().placa().matches("[A-Z]{3}\\d[A-Z0-9]\\d{2}"), "Placa inválida"));
    final SegmentoEmMemoria<DadosServico> servico = new SegmentoEmMemoria<>("SERVICO", "SRV", DadosServico.class,
            s -> java.util.Optional.empty());
    final AtomicLong sequencia = new AtomicLong();
    final List<ProcessoCriado> publicados = new ArrayList<>();
    final MotorDeProcessamento motor = new MotorDeProcessamento(List.of(auto, servico), sequencia::incrementAndGet,
            Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC), publicados::add);

    @Test
    void mesmoMotorAtendeTodosOsSegmentosComNumeracaoUnica() {
        var carro = motor.criar(new Solicitacao<>(ANA, new BigDecimal("50000"), new DadosAuto("ABC1D23")));
        var reforma = motor.criar(new Solicitacao<>(ANA, new BigDecimal("8000"), new DadosServico("Reforma")));

        assertEquals("AUT-2026-000001", carro.numero());
        assertEquals("SRV-2026-000002", reforma.numero());
        assertEquals(List.of(carro), auto.gravados);
        assertEquals(List.of(reforma), servico.gravados);
        assertEquals(List.of(new ProcessoCriado(carro), new ProcessoCriado(reforma)), publicados);
    }

    @Test
    void processoNasceAbertoComOsDadosDaSolicitacao() {
        var processo = motor.criar(new Solicitacao<>(ANA, new BigDecimal("50000"), new DadosAuto("ABC1D23")));

        assertEquals("AUTO", processo.segmento());
        assertEquals(Processo.Situacao.ABERTO, processo.situacao());
        assertEquals(HOJE, processo.dataAbertura());
        assertEquals(ANA, processo.proponente());
        assertEquals(new DadosAuto("ABC1D23"), processo.dados());
    }

    @Test
    void regraDoSegmentoVioladaNaoGravaNemPublica() {
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(ANA, new BigDecimal("50000"), new DadosAuto("placa"))));

        assertEquals("Placa inválida", erro.getMessage());
        assertTrue(auto.gravados.isEmpty());
        assertTrue(publicados.isEmpty());
        assertEquals(0, sequencia.get());
    }

    @Test
    void regrasComunsVemAntesDasDoSegmento() {
        var semNome = new Proponente("", "52998224725");
        var erro = assertThrows(RegraVioladaException.class,
                () -> motor.criar(new Solicitacao<>(semNome, new BigDecimal("50000"), new DadosAuto("placa"))));

        assertEquals("Nome do proponente é obrigatório", erro.getMessage());
    }

    @Test
    void dadosSemSegmentoRegistrado() {
        record DadosRural(String car) implements DadosDoSegmento {
        }
        assertThrows(IllegalArgumentException.class,
                () -> motor.criar(new Solicitacao<>(ANA, BigDecimal.TEN, new DadosRural("x"))));
    }

    @Test
    void segmentoRegistradoDuasVezes() {
        var outroAuto = new SegmentoEmMemoria<>("AUTO", "AUT", DadosServico.class, s -> java.util.Optional.empty());
        assertThrows(IllegalStateException.class, () -> new MotorDeProcessamento(List.of(auto, outroAuto),
                sequencia::incrementAndGet, Clock.systemUTC(), publicados::add));
    }
}
