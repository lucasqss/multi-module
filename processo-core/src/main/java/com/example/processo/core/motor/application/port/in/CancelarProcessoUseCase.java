package com.example.processo.core.motor.application.port.in;

import com.example.processo.core.motor.domain.model.Processo;

import java.util.UUID;

/** ABERTO → CANCELADO. */
public interface CancelarProcessoUseCase {

    Processo<?> cancelar(UUID id);
}
