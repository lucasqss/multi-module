package com.example.processo.persistencia.repositorio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Liga a chave de idempotência de quem pediu a criação (ex.: id da mensagem) ao processo criado. Tabela técnica, não
 * vocabulário do negócio: só o {@link ProcessoRepository} a conhece.
 */
@Entity
@Table(name = "PROCESSO_IDEMPOTENCIA")
class ChaveIdempotencia {

    @Id
    @Column(length = 100)
    private String chave;

    @Column(name = "PROCESSO_ID", nullable = false)
    private UUID processoId;

    protected ChaveIdempotencia() {
    }

    ChaveIdempotencia(String chave, UUID processoId) {
        this.chave = chave;
        this.processoId = processoId;
    }

    String getChave() { return chave; }
    UUID getProcessoId() { return processoId; }
}
