package com.example.boleto.adapter.database;

import com.example.boleto.domain.state.StatusBoleto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de PERSISTÊNCIA: anêmico de propósito, é só o formato da tabela.
 * Fica no adapter para que o domínio não dependa de JPA.
 */
@Entity
@Table(name = "boleto", indexes = @Index(name = "ix_boleto_pagador_status", columnList = "pagador_documento, status"))
public class BoletoJpaEntity {

    @Id
    public UUID id;

    /** Lock otimista: duas alterações concorrentes no mesmo boleto não se sobrescrevem. */
    @Version
    public long versao;

    @Column(name = "pagador_nome", nullable = false)
    public String pagadorNome;

    @Column(name = "pagador_documento", nullable = false, length = 14)
    public String pagadorDocumento;

    @Column(name = "pagador_email", nullable = false)
    public String pagadorEmail;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @Column(name = "linha_digitavel", nullable = false, length = 47)
    public String linhaDigitavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusBoleto status;

    @Column(name = "emitido_em", nullable = false, updatable = false)
    public Instant emitidoEm;

    @Column(name = "notificado_em")
    public Instant notificadoEm;

    @Column(name = "valor_pago", precision = 15, scale = 2)
    public BigDecimal valorPago;

    @Column(name = "data_pagamento")
    public LocalDate dataPagamento;

    @Column(name = "motivo_cancelamento")
    public String motivoCancelamento;

    /** Auditoria técnica: preenchida pelo Hibernate, o domínio não precisa saber. */
    @UpdateTimestamp
    @Column(name = "atualizado_em")
    public Instant atualizadoEm;
}
