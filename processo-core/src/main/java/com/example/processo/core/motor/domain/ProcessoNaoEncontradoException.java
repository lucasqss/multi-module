package com.example.processo.core.motor.domain;

import java.util.UUID;

public class ProcessoNaoEncontradoException extends RuntimeException {

    public ProcessoNaoEncontradoException(UUID id) {
        super("Processo " + id + " não encontrado");
    }
}
