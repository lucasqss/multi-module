package com.example.processo.motor.domain;

/**
 * Publicado pelo motor depois de criar um processo. Quem precisa reagir (impressão, notificações) observa o
 * evento; o motor não sabe quem são.
 */
public record ProcessoCriado(Processo<?> processo) {
}
