package com.example.processo.core.segmento.veicular.application;

import com.example.processo.core.Fakes;
import com.example.processo.core.motor.application.MotorDeProcessamento;
import com.example.processo.core.motor.domain.Avaliacao;
import com.example.processo.core.motor.domain.Processo;
import com.example.processo.core.motor.domain.Proponente;
import com.example.processo.core.motor.domain.RegraVioladaException;
import com.example.processo.core.motor.domain.Solicitacao;
import com.example.processo.core.segmento.veicular.domain.DadosVeiculares;
import com.example.processo.core.segmento.veicular.domain.ProcessosVeiculares;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** O segmento veicular plugado no motor real, sem Quarkus nem banco. */
class SegmentoVeicularTest {

    static final LocalDate HOJE = LocalDate.of(2026, 10, 19);
    static final Clock RELOGIO = Clock.fixed(Instant.from(HOJE.atStartOfDay(ZoneOffset.UTC)), ZoneOffset.UTC);
    static final Proponente ANA = new Proponente("Ana Souza", "52998224725");

    static class ProcessosEmMemoria implements ProcessosVeiculares {
        final List<Processo<DadosVeiculares>> dados = new ArrayList<>();

        public void salvar(Processo<DadosVeiculares> processo) { dados.add(processo); }

        public Optional<Processo<DadosVeiculares>> porId(UUID id) {
            return dados.stream().filter(p -> p.id().equals(id)).findFirst();
        }
    }

    final ProcessosEmMemoria processos = new ProcessosEmMemoria();
    final SegmentoVeicular segmento = new SegmentoVeicular(processos, RELOGIO);
    final MotorDeProcessamento motor = new MotorDeProcessamento(List.of(segmento), cpf -> true,
            new AtomicLong(6)::incrementAndGet, new Fakes.BancoEmMemoria(), Fakes.semTransacao(), RELOGIO, p -> { });

    static Solicitacao<DadosVeiculares> solicitacao(String valor, String placa, Integer ano, String fipe) {
        return new Solicitacao<>(ANA, new BigDecimal(valor),
                new DadosVeiculares(placa, ano, fipe == null ? null : new BigDecimal(fipe)));
    }

    String motivo(Solicitacao<DadosVeiculares> solicitacao) {
        return assertThrows(RegraVioladaException.class, () -> motor.criar(solicitacao)).getMessage();
    }

    @Test
    void criaProcessoVeicularPeloMesmoMotor() {
        var processo = motor.criar(solicitacao("45000.00", "ABC1D23", 2022, "50000.00"));

        assertEquals("VEI-2026-000007", processo.numero());
        assertEquals(SegmentoVeicular.CODIGO, processo.segmento());
        assertEquals(List.of(processo), processos.dados);
        assertEquals(Optional.of(processo), segmento.carregar(processo.id()));
    }

    @Test
    void placaAntigaEMercosulSaoAceitas() {
        motor.criar(solicitacao("1000", "ABC1234", 2022, "50000"));
        motor.criar(solicitacao("1000", "ABC1D23", 2022, "50000"));
        assertEquals(2, processos.dados.size());
    }

    @Test
    void dadosDoVeiculoInvalidos() {
        assertEquals("Placa inválida", motivo(solicitacao("1000", "abc-1234", 2022, "50000")));
        assertEquals("Ano de fabricação inválido", motivo(solicitacao("1000", "ABC1234", null, "50000")));
        assertEquals("Valor FIPE deve ser maior que zero", motivo(solicitacao("1000", "ABC1234", 2022, "0")));
        assertEquals("Valor excede 90% da FIPE (máximo 45000.00)",
                motivo(solicitacao("45000.01", "ABC1234", 2022, "50000")));
        assertTrue(processos.dados.isEmpty());
    }

    @Test
    void processamentoRecusaVeiculoComMaisDeDezAnos() {
        var dezAnos = motor.criar(solicitacao("1000", "ABC1234", 2016, "50000"));
        var onzeAnos = motor.criar(solicitacao("1000", "ABC1234", 2015, "50000"));

        assertEquals(Avaliacao.aprovado(), segmento.avaliar(dezAnos));
        assertEquals(Avaliacao.recusado("Veículo com mais de 10 anos (fabricado em 2015)"), segmento.avaliar(onzeAnos));
    }
}
