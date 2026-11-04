package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.adapter.banco.BancoPagamentos;

import java.time.Clock;

/** O que os comandos podem precisar, injetado uma vez no invocador e repassado. */
public record ContextoComando(Clock clock, BancoPagamentos banco) {
}
