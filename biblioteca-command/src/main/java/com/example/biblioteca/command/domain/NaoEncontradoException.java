package com.example.biblioteca.command.domain;

import java.util.UUID;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, UUID id) {
        super(recurso + " " + id + " não encontrado");
    }
}
