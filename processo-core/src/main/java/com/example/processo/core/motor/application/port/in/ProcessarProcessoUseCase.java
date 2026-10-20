package com.example.processo.core.motor.application.port.in;

import com.example.processo.core.motor.domain.model.Processo;

import java.util.UUID;

/** ABERTO → EM_PROCESSAMENTO → CONCLUIDO ou RECUSADO, conforme a avaliação do segmento. */
public interface ProcessarProcessoUseCase {

    Processo<?> processar(UUID id);
}
