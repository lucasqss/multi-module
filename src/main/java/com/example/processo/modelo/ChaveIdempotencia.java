package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Liga a chave de idempotência de quem pediu a criação (ex.: id da mensagem) ao processo criado. */
@Entity
@Table(name = "PROCESSO_IDEMPOTENCIA")
public class ChaveIdempotencia {

    @Id
    @Column(length = 100)
    private String chave;

    @Column(name = "PROCESSO_ID", nullable = false)
    private UUID processoId;

    protected ChaveIdempotencia() {
    }

    public ChaveIdempotencia(String chave, UUID processoId) {
        this.chave = chave;
        this.processoId = processoId;
    }

    public String getChave() { return chave; }
    public UUID getProcessoId() { return processoId; }
}
