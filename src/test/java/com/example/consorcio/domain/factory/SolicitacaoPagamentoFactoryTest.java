package com.example.consorcio.domain.factory;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.SituacaoCota;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.valueobject.DadosPagamento;
import com.example.consorcio.domain.valueobject.Dinheiro;
import com.example.consorcio.domain.valueobject.IdentificadorCota;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoPagamentoFactoryTest {

    private static final Cota COTA = new Cota(new IdentificadorCota(1, 1), SituacaoCota.ATIVA, Dinheiro.de("1000"));
    private static final DadosPagamento DADOS =
            new DadosPagamento(Dinheiro.de("100"), FormaPagamento.PIX, LocalDate.of(2026, 11, 10), null);

    @Test
    void hojeEhCalculadoNoFusoDoNegocioENaoEmUtc() {
        // 01:00 UTC do dia 04 = 22:00 do dia 03 em São Paulo
        var instante = Instant.parse("2026-11-04T01:00:00Z");
        var factory = new SolicitacaoPagamentoFactory(Clock.fixed(instante, ZoneId.of("America/Sao_Paulo")), 30);

        var solicitacao = factory.paraAbertura(COTA, DADOS, false);

        assertEquals(LocalDate.of(2026, 11, 3), solicitacao.hoje());
        assertEquals(instante, solicitacao.momento());
    }

    @Test
    void fatoDeProcessoEmAndamentoSoValeNaAbertura() {
        var factory = new SolicitacaoPagamentoFactory(Clock.systemUTC(), 30);

        assertTrue(factory.paraAbertura(COTA, DADOS, true).cotaPossuiProcessoEmAndamento());
        assertFalse(factory.paraAlteracao(COTA, DADOS).cotaPossuiProcessoEmAndamento());
    }
}
