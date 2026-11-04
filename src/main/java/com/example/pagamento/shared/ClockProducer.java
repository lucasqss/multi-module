package com.example.pagamento.shared;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Fornece o {@link Clock} da aplicação. Fuso EXPLÍCITO: "hoje" é a data no fuso do negócio,
 * não no do servidor (containers costumam rodar em UTC, onde 21h de Brasília já é "amanhã").
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    @ApplicationScoped
    Clock clock(@ConfigProperty(name = "pagamento.fuso-horario") ZoneId fusoHorario) {
        return Clock.system(fusoHorario);
    }
}
