package com.example.boleto.domain.specification.boleto;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * O que vem de FORA do boleto no momento da emissão: relógio (no fuso do negócio),
 * parâmetros configuráveis e fatos consultados pelo use case no repositório.
 */
public record CondicoesEmissao(Instant momento, LocalDate hoje, int prazoMaximoVencimentoDias,
                               long boletosEmAbertoDoPagador) {

    public CondicoesEmissao {
        Objects.requireNonNull(momento);
        Objects.requireNonNull(hoje);
    }

    public static CondicoesEmissao de(Clock clock, int prazoMaximoVencimentoDias, long boletosEmAbertoDoPagador) {
        var agora = clock.instant();
        return new CondicoesEmissao(agora, LocalDate.ofInstant(agora, clock.getZone()),
                prazoMaximoVencimentoDias, boletosEmAbertoDoPagador);
    }
}
