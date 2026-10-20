package com.example.processo.core.motor.domain.event;

import com.example.processo.core.motor.domain.model.Processo;

/**
 * Publicado pelo motor depois de criar um processo. Quem precisa reagir (impressão, notificações) observa o
 * evento; o motor não sabe quem são.
 */
public record ProcessoCriado(Processo<?> processo) {
}
