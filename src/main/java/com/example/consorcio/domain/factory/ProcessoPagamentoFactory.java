package com.example.consorcio.domain.factory;

import com.example.consorcio.domain.entity.ProcessoPagamento;
import com.example.consorcio.domain.model.Cota;
import com.example.consorcio.domain.model.SolicitacaoPagamento;
import com.example.consorcio.domain.valueobject.DadosPagamento;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Monta as {@link SolicitacaoPagamento} (o candidato das specifications) com o que vem
 * de infraestrutura: relógio e parâmetros configuráveis. Assim nem a entidade nem as
 * regras conhecem {@code Clock} ou {@code application.properties}.
 *
 * <p>Classe Java pura; disponibilizada ao CDI por {@code shared.config.DomainBeansProducer}.
 */
public class ProcessoPagamentoFactory {

    private final Clock clock;
    private final int prazoMaximoVencimentoDias;

    public ProcessoPagamentoFactory(Clock clock, int prazoMaximoVencimentoDias) {
        this.clock = clock;
        this.prazoMaximoVencimentoDias = prazoMaximoVencimentoDias;
    }

    public ProcessoPagamento abrir(Cota cota, DadosPagamento dados, boolean cotaPossuiProcessoEmAndamento) {
        var solicitacao = new SolicitacaoPagamento(cota, dados, LocalDate.now(clock),
                prazoMaximoVencimentoDias, cotaPossuiProcessoEmAndamento);
        return ProcessoPagamento.abrir(solicitacao, clock.instant());
    }

    public SolicitacaoPagamento solicitacaoDeAlteracao(Cota cota, DadosPagamento dados) {
        // na alteração o próprio processo é "o que está em andamento", então o fato não se aplica
        return new SolicitacaoPagamento(cota, dados, LocalDate.now(clock), prazoMaximoVencimentoDias, false);
    }
}
