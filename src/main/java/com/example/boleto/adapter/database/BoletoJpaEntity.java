package com.example.boleto.adapter.database;

import com.example.boleto.domain.state.StatusBoleto;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo de PERSISTÊNCIA: anêmico de propósito, é só o formato das tabelas.
 * Fica no adapter para que o domínio não dependa de JPA.
 *
 * <p>As partes do agregado (itens, destinatários) são coleções da própria linha do boleto
 * ({@code @ElementCollection}): são gravadas e apagadas junto com ele, sem repositório próprio.
 */
@Entity
@Table(name = "boleto", indexes = {
        @Index(name = "ix_boleto_pagador_status", columnList = "pagador_documento, status"),
        @Index(name = "ux_boleto_linha_digitavel", columnList = "linha_digitavel", unique = true)})
public class BoletoJpaEntity {

    @Id
    public UUID id;

    /** Lock otimista: duas edições concorrentes do mesmo boleto (mesmo em seções diferentes) não se sobrescrevem. */
    @Version
    public long versao;

    @Column(name = "pagador_nome", nullable = false)
    public String pagadorNome;

    @Column(name = "pagador_documento", nullable = false, length = 14)
    public String pagadorDocumento;

    @Column(name = "pagador_email", nullable = false)
    public String pagadorEmail;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "boleto_item", joinColumns = @JoinColumn(name = "boleto_id"))
    @OrderColumn(name = "ordem")
    public List<ItemJpa> itens = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "boleto_destinatario", joinColumns = @JoinColumn(name = "boleto_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "email", nullable = false)
    public List<String> destinatarios = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusBoleto status;

    @Column(name = "linha_digitavel", length = 47)
    public String linhaDigitavel;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public Instant criadoEm;

    @Column(name = "emitido_em")
    public Instant emitidoEm;

    @Column(name = "notificado_em")
    public Instant notificadoEm;

    @Column(name = "id_transacao_pagamento", length = 64)
    public String idTransacaoPagamento;

    @Column(name = "valor_pago", precision = 15, scale = 2)
    public BigDecimal valorPago;

    @Column(name = "data_pagamento")
    public LocalDate dataPagamento;

    @Column(name = "motivo_analise_codigo", length = 60)
    public String motivoAnaliseCodigo;

    @Column(name = "motivo_analise_mensagem")
    public String motivoAnaliseMensagem;

    @Column(name = "motivo_devolucao")
    public String motivoDevolucao;

    @Column(name = "motivo_cancelamento")
    public String motivoCancelamento;

    /** Auditoria técnica: preenchida pelo Hibernate, o domínio não precisa saber. */
    @UpdateTimestamp
    @Column(name = "atualizado_em")
    public Instant atualizadoEm;

    @Embeddable
    public static class ItemJpa {

        @Column(nullable = false, length = 100)
        public String descricao;

        @Column(nullable = false, precision = 15, scale = 2)
        public BigDecimal valor;

        public ItemJpa() {
        }

        ItemJpa(String descricao, BigDecimal valor) {
            this.descricao = descricao;
            this.valor = valor;
        }
    }
}
