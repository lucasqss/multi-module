package com.example.processo.core.segmento.imobiliario.application;

import com.example.processo.core.segmento.imobiliario.domain.DadosImobiliarios;
import com.example.processo.core.segmento.imobiliario.domain.DadosImobiliarios.TipoImovel;
import com.example.processo.core.segmento.imobiliario.domain.ProcessosImobiliarios;
import com.example.processo.core.motor.application.MotorDeProcessamento;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.ProcessoCriado;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.RegraVioladaException;
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

/** O segmento plugado no motor real, sem Quarkus nem banco: o port de persistência é um fake em memória. */
class SegmentoImobiliarioTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Proponente ANA = new Proponente("Ana Souza", "52998224725");

    static class ProcessosEmMemoria implements ProcessosImobiliarios {
        final List<Processo<DadosImobiliarios>> dados = new ArrayList<>();

        public boolean existeAbertoParaMatricula(String matricula, String uf) {
            return dados.stream().anyMatch(p -> p.situacao() == Processo.Situacao.ABERTO
                    && p.dados().matricula().equals(matricula) && p.dados().uf().equals(uf));
        }

        public void salvar(Processo<DadosImobiliarios> processo) { dados.add(processo); }
    }

    final ProcessosEmMemoria processos = new ProcessosEmMemoria();
    final List<ProcessoCriado> publicados = new ArrayList<>();
    final MotorDeProcessamento motor = new MotorDeProcessamento(List.of(new SegmentoImobiliario(processos)),
            cpf -> true, new AtomicLong(41)::incrementAndGet, Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)),
            ZoneOffset.UTC), publicados::add);

    static Solicitacao<DadosImobiliarios> solicitacao(String valor, TipoImovel tipo, String avaliacao) {
        return solicitacao(valor, new DadosImobiliarios("12345", tipo, new BigDecimal(avaliacao), "SP"));
    }

    static Solicitacao<DadosImobiliarios> solicitacao(String valor, DadosImobiliarios imovel) {
        return new Solicitacao<>(ANA, new BigDecimal(valor), imovel);
    }

    String motivo(Solicitacao<DadosImobiliarios> solicitacao) {
        return assertThrows(RegraVioladaException.class, () -> motor.criar(solicitacao)).getMessage();
    }

    @Test
    void criaProcessoImobiliarioPeloMotor() {
        var processo = motor.criar(solicitacao("400000.00", TipoImovel.RESIDENCIAL, "500000.00"));

        assertEquals("IMO-2026-000042", processo.numero());
        assertEquals(SegmentoImobiliario.CODIGO, processo.segmento());
        assertEquals(List.of(processo), processos.dados);
        assertEquals(List.of(new ProcessoCriado(processo)), publicados);
    }

    @Test
    void limiteDaAvaliacaoDependeDoTipoDoImovel() {
        motor.criar(solicitacao("300000.00", TipoImovel.COMERCIAL, "500000.00"));

        assertEquals("Valor excede 80% da avaliação para imóvel residencial (máximo 400000.00)",
                motivo(solicitacao("400000.01", TipoImovel.RESIDENCIAL, "500000.00")));
        assertEquals("Valor excede 60% da avaliação para imóvel comercial (máximo 300000.00)",
                motivo(solicitacao("300000.01", TipoImovel.COMERCIAL, "500000.00")));
    }

    @Test
    void matriculaComProcessoAbertoNaMesmaUf() {
        motor.criar(solicitacao("100000", new DadosImobiliarios("777", TipoImovel.RESIDENCIAL, new BigDecimal("500000"), "SP")));

        assertEquals("Já existe processo aberto para a matrícula 777/SP", motivo(
                solicitacao("100000", new DadosImobiliarios("777", TipoImovel.RESIDENCIAL, new BigDecimal("500000"), "SP"))));
        motor.criar(solicitacao("100000", new DadosImobiliarios("777", TipoImovel.RESIDENCIAL, new BigDecimal("500000"), "RJ")));
        assertEquals(2, processos.dados.size());
    }

    @Test
    void dadosDoImovelInvalidos() {
        var avaliacao = new BigDecimal("500000");
        assertEquals("Matrícula do imóvel deve ter de 1 a 20 dígitos",
                motivo(solicitacao("1000", new DadosImobiliarios("12-A", TipoImovel.RESIDENCIAL, avaliacao, "SP"))));
        assertEquals("UF do imóvel inválida",
                motivo(solicitacao("1000", new DadosImobiliarios("12", TipoImovel.RESIDENCIAL, avaliacao, "XX"))));
        assertEquals("Tipo do imóvel é obrigatório",
                motivo(solicitacao("1000", new DadosImobiliarios("12", null, avaliacao, "SP"))));
        assertEquals("Valor de avaliação do imóvel deve ser maior que zero",
                motivo(solicitacao("1000", new DadosImobiliarios("12", TipoImovel.COMERCIAL, BigDecimal.ZERO, "SP"))));
        assertTrue(processos.dados.isEmpty());
    }

    @Test
    void regrasComunsDoMotorValemParaOSegmento() {
        var cpfInvalido = new Solicitacao<>(new Proponente("Ana Souza", "12345678900"), new BigDecimal("1000"),
                new DadosImobiliarios("12", TipoImovel.RESIDENCIAL, new BigDecimal("500000"), "SP"));
        assertEquals("CPF inválido: 12345678900", motivo(cpfInvalido));
    }
}
