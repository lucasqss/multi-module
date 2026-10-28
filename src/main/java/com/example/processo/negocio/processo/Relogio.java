package com.example.processo.negocio.processo;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/** O negócio recebe o relógio em vez de chamar LocalDate.now(): testes podem fixar a data. */
public class Relogio {

    @Produces
    @Singleton
    Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
