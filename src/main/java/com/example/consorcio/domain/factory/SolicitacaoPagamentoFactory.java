package com.example.consorcio.domain.factory;

import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.valueobject.DadosPagamento;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Responsabilidade única: montar a {@link SolicitacaoPagamento} (o candidato das specifications)
 * com o que vem de infraestrutura: relógio e parâmetros configuráveis. Assim nem a entidade nem as
 * regras conhecem {@code Clock} ou {@code application.properties}.
 *
 * <p>Classe Java pura; disponibilizada ao CDI por {@code shared.config.DomainBeansProducer}.
 */
public class SolicitacaoPagamentoFactory {

    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public SolicitacaoPagamentoFactory(Clock clock, int prazoMaximoVencimentoDias) {
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    public SolicitacaoPagamento paraAbertura(Cota cota, DadosPagamento dados, boolean cotaPossuiProcessoEmAndamento) {
        return criar(cota, dados, cotaPossuiProcessoEmAndamento);
    }

    public SolicitacaoPagamento paraAlteracao(Cota cota, DadosPagamento dados) {
        // na alteração o próprio processo é "o que está em andamento", então o fato não se aplica
        return criar(cota, dados, false);
    }

    private SolicitacaoPagamento criar(Cota cota, DadosPagamento dados, boolean possuiProcessoEmAndamento) {
        var agora = clock.instant();
        return new SolicitacaoPagamento(cota, dados, agora, LocalDate.ofInstant(agora, clock.getZone()),
                prazoMaximoVencimentoDias, possuiProcessoEmAndamento);
    }
}
