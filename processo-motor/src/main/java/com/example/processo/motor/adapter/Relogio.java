package com.example.processo.motor.adapter;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/** O motor recebe o relógio em vez de chamar LocalDate.now(): testes fixam a data. */
public class Relogio {

    @Produces
    @Singleton
    Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
