package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.state.StatusPagamento;
import com.example.pagamento.domain.valueobject.Modalidade;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de PERSISTÊNCIA: anêmico de propósito, é só o formato das tabelas.
 * A seção de boletos é uma coleção da própria linha ({@code @ElementCollection}): gravada junto com o pagamento.
 * Sem {@code @Version}: a concorrência é tratada com lock PESSIMISTA no repositório.
 */
@Entity
@Table(name = "pagamento", indexes = @Index(name = "ux_pagamento_transacao", columnList = "id_transacao", unique = true))
public class PagamentoJpaEntity {

    @Id
    public UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Modalidade modalidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public Instant criadoEm;

    /**
     * LAZY porque FOR UPDATE não combina com join de coleção. {@code @BatchSize}: ao listar, os boletos de
     * vários pagamentos vêm numa consulta só (sem N+1). O Quarkus já usa lote 16 por padrão; 100 = página máxima.
     */
    @ElementCollection
    @BatchSize(size = 100)
    @CollectionTable(name = "pagamento_boleto", joinColumns = @JoinColumn(name = "pagamento_id"))
    @OrderColumn(name = "ordem")
    public List<BoletoJpa> boletos = new ArrayList<>();

    @Column(name = "agencia_debito", length = 4)
    public String agenciaDebito;

    @Column(name = "conta_debito", length = 12)
    public String contaDebito;

    @Column(name = "data_pagamento")
    public LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPagamento status;

    @Column(nullable = false)
    public int envios;

    @Column(name = "id_transacao", length = 50)
    public String idTransacao;

    @Column(length = 60)
    public String autenticacao;

    @Column(name = "motivo_recusa")
    public String motivoRecusa;

    @Column(name = "motivo_analise_codigo", length = 60)
    public String motivoAnaliseCodigo;

    @Column(name = "motivo_analise_mensagem")
    public String motivoAnaliseMensagem;

    @Column(name = "motivo_cancelamento")
    public String motivoCancelamento;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    public Instant atualizadoEm;

    @Embeddable
    public static class BoletoJpa {
        @Column(name = "id_no_emissor", nullable = false, length = 40)
        public String idNoEmissor;
        @Column(name = "linha_digitavel", nullable = false, length = 47)
        public String linhaDigitavel;
        @Column(nullable = false, precision = 15, scale = 2)
        public BigDecimal valor;
        @Column(nullable = false)
        public LocalDate vencimento;
        @Column(nullable = false)
        public String beneficiario;
    }
}
