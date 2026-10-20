package com.example.processo.core.motor.application;

import java.util.function.Supplier;

/**
 * Demarcação de transação explícita e curta: o motor deixa de fora o que pode demorar (regras que chamam
 * sistema externo, avaliação do segmento) e só põe na transação a gravação. Nos testes, executa direto.
 */
public interface Transacoes {

    <T> T executar(Supplier<T> trabalho);
}
