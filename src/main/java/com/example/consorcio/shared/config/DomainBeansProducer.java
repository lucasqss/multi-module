package com.example.consorcio.shared.config;

import com.example.consorcio.domain.factory.SolicitacaoPagamentoFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Liga objetos de domínio (Java puro) ao CDI, para o domínio não precisar de anotações de framework.
 */
@ApplicationScoped
public class DomainBeansProducer {

    /**
     * Fuso EXPLÍCITO: "hoje" é a data no fuso do negócio, não no do servidor
     * (containers costumam rodar em UTC, onde 21h de Brasília já é "amanhã").
     */
    @Produces
    @ApplicationScoped
    Clock clock(@ConfigProperty(name = "consorcio.fuso-horario") ZoneId fusoHorario) {
        return Clock.system(fusoHorario);
    }

    @Produces
    @ApplicationScoped
    SolicitacaoPagamentoFactory solicitacaoPagamentoFactory(
            Clock clock,
            @ConfigProperty(name = "consorcio.pagamento.prazo-maximo-vencimento-dias") int prazoMaximo) {
        return new SolicitacaoPagamentoFactory(clock, prazoMaximo);
    }
}
