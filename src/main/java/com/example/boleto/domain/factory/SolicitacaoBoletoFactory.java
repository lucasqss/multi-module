package com.example.boleto.domain.factory;

import com.example.boleto.domain.model.SolicitacaoBoleto;
import com.example.boleto.domain.valueobject.DadosBoleto;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Responsabilidade única: montar a {@link SolicitacaoBoleto} (o candidato das specifications)
 * com o que vem de infraestrutura: relógio e parâmetros configuráveis. Assim nem a entidade nem as
 * regras conhecem {@code Clock} ou {@code application.properties}.
 *
 * <p>Classe Java pura; disponibilizada ao CDI por {@code shared.config.DomainBeansProducer}.
 */
public class SolicitacaoBoletoFactory {

    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public SolicitacaoBoletoFactory(Clock clock, int prazoMaximoVencimentoDias) {
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    public SolicitacaoBoleto paraEmissao(DadosBoleto dados, long boletosEmAbertoDoPagador) {
        return criar(dados, boletosEmAbertoDoPagador);
    }

    public SolicitacaoBoleto paraAlteracao(DadosBoleto dados) {
        // o limite de boletos em aberto só vale para EMITIR um novo; na alteração o fato não se aplica
        return criar(dados, 0);
    }

    private SolicitacaoBoleto criar(DadosBoleto dados, long boletosEmAberto) {
        var agora = clock.instant();
        return new SolicitacaoBoleto(dados, agora, LocalDate.ofInstant(agora, clock.getZone()),
                prazoMaximoVencimentoDias, boletosEmAberto);
    }
}
