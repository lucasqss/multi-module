package com.example.biblioteca.command.adapter;

import jakarta.inject.Singleton;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

/** O domínio recebe o relógio em vez de chamar LocalDate.now(): testes fixam a data. */
public class Relogio {

    @Produces
    @Singleton
    Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
