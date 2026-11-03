package com.example.boleto.domain.specification;

import com.example.boleto.domain.specification.boleto.CondicoesEmissao;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CondicoesEmissaoTest {

    @Test
    void hojeEhCalculadoNoFusoDoNegocioENaoEmUtc() {
        // 01:00 UTC do dia 04 = 22:00 do dia 03 em São Paulo
        var instante = Instant.parse("2026-11-04T01:00:00Z");
        var clock = Clock.fixed(instante, ZoneId.of("America/Sao_Paulo"));

        var condicoes = CondicoesEmissao.de(clock, 60, 2);

        assertEquals(LocalDate.of(2026, 11, 3), condicoes.hoje());
        assertEquals(instante, condicoes.momento());
        assertEquals(2, condicoes.boletosEmAbertoDoPagador());
    }
}
