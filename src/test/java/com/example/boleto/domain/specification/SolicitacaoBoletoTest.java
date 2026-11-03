package com.example.boleto.domain.specification;

import com.example.boleto.domain.specification.boleto.SolicitacaoBoleto;
import com.example.boleto.domain.valueobject.DadosBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagador;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SolicitacaoBoletoTest {

    private static final DadosBoleto DADOS = new DadosBoleto(
            Pagador.de("Maria", "12345678901", "maria@example.com"), Dinheiro.de("100"), LocalDate.of(2026, 11, 10));

    @Test
    void hojeEhCalculadoNoFusoDoNegocioENaoEmUtc() {
        // 01:00 UTC do dia 04 = 22:00 do dia 03 em São Paulo
        var instante = Instant.parse("2026-11-04T01:00:00Z");
        var clock = Clock.fixed(instante, ZoneId.of("America/Sao_Paulo"));

        var solicitacao = SolicitacaoBoleto.paraEmissao(DADOS, clock, 60, 0);

        assertEquals(LocalDate.of(2026, 11, 3), solicitacao.hoje());
        assertEquals(instante, solicitacao.momento());
    }

    @Test
    void contagemDeBoletosEmAbertoSoValeNaEmissao() {
        var clock = Clock.systemUTC();

        assertEquals(2, SolicitacaoBoleto.paraEmissao(DADOS, clock, 60, 2).boletosEmAbertoDoPagador());
        assertEquals(0, SolicitacaoBoleto.paraAlteracao(DADOS, clock, 60).boletosEmAbertoDoPagador());
    }
}
