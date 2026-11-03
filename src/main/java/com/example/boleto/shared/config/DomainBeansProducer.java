package com.example.boleto.shared.config;

import com.example.boleto.domain.factory.SolicitacaoBoletoFactory;
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
    Clock clock(@ConfigProperty(name = "boleto.fuso-horario") ZoneId fusoHorario) {
        return Clock.system(fusoHorario);
    }

    @Produces
    @ApplicationScoped
    SolicitacaoBoletoFactory solicitacaoBoletoFactory(
            Clock clock,
            @ConfigProperty(name = "boleto.prazo-maximo-vencimento-dias") int prazoMaximo) {
        return new SolicitacaoBoletoFactory(clock, prazoMaximo);
    }
}
