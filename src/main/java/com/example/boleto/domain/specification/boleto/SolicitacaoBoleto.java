package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.valueobject.DadosBoleto;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO avaliado pelas specifications de emissão/alteração.
 *
 * <p>Reúne tudo o que as regras precisam saber, já resolvido: os dados pedidos, o momento da
 * solicitação, parâmetros configuráveis e fatos consultados no repositório.
 * Assim as specifications são funções puras: sem banco, sem relógio, sem framework.
 *
 * <p>Fica junto das specs porque só existe para elas. Criada pelos métodos estáticos abaixo,
 * que calculam "hoje" no fuso do {@link Clock} recebido (o fuso do negócio).
 */
public record SolicitacaoBoleto(
        DadosBoleto dados,
        Instant momento,
        LocalDate hoje,
        int prazoMaximoVencimentoDias,
        long boletosEmAbertoDoPagador) {

    public SolicitacaoBoleto {
        Objects.requireNonNull(dados);
        Objects.requireNonNull(momento);
        Objects.requireNonNull(hoje);
    }

    public static SolicitacaoBoleto paraEmissao(DadosBoleto dados, Clock clock, int prazoMaximoVencimentoDias,
                                                long boletosEmAbertoDoPagador) {
        var agora = clock.instant();
        return new SolicitacaoBoleto(dados, agora, LocalDate.ofInstant(agora, clock.getZone()),
                prazoMaximoVencimentoDias, boletosEmAbertoDoPagador);
    }

    /** O limite de boletos em aberto só vale para EMITIR um novo; na alteração o fato não se aplica. */
    public static SolicitacaoBoleto paraAlteracao(DadosBoleto dados, Clock clock, int prazoMaximoVencimentoDias) {
        return paraEmissao(dados, clock, prazoMaximoVencimentoDias, 0);
    }
}
