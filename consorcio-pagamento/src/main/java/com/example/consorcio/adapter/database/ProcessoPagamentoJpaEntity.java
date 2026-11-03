package com.example.consorcio.adapter.database;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.enums.StatusProcessoPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de PERSISTÊNCIA (anêmico de propósito: é só o formato da tabela).
 * Fica no adapter para que o domínio não dependa de JPA.
 */
@Entity
@Table(name = "processo_pagamento")
public class ProcessoPagamentoJpaEntity {

    @Id
    public UUID id;

    @Column(nullable = false)
    public int grupo;

    @Column(nullable = false)
    public int cota;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    public FormaPagamento formaPagamento;

    @Column(name = "agencia_debito")
    public String agenciaDebito;

    @Column(name = "conta_debito")
    public String contaDebito;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StatusProcessoPagamento status;

    @Column(name = "motivo_cancelamento")
    public String motivoCancelamento;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;
}
