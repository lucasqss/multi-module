package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.adapter.registro.RegistroDeGarantias;

import java.time.Clock;

/** O que os comandos podem precisar, injetado uma vez no invocador e repassado. */
public record ContextoComando(Clock clock, RegistroDeGarantias registro) {
}
