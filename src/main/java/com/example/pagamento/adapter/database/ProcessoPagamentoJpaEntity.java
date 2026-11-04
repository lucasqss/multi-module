package com.example.pagamento.adapter.database;

import com.example.pagamento.domain.state.StatusProcesso;
import com.example.pagamento.domain.valueobject.StatusTentativa;
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
 * Modelo de PERSISTÊNCIA do agregado: anêmico de propósito, é só o formato das tabelas.
 * Partes e entidades internas (boletos, aprovações, tentativas) são coleções da própria linha do processo
 * ({@code @ElementCollection}): gravadas e apagadas junto com ele, sem repositório próprio.
 */
@Entity
@Table(name = "processo_pagamento", indexes = @Index(name = "ix_processo_solicitante_status", columnList = "solicitante, status"))
public class ProcessoPagamentoJpaEntity {

    @Id
    public UUID id;

    /**
     * Lock otimista no agregado inteiro: edições concorrentes (mesmo de partes diferentes) não se
     * sobrescrevem.
     */
    @Version
    public long versao;

    @Column(nullable = false, length = 8)
    public String solicitante;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public Instant criadoEm;

    @Column(name = "agencia_debito", length = 4)
    public String agenciaDebito;

    @Column(name = "conta_debito", length = 12)
    public String contaDebito;

    @Column(name = "data_pagamento")
    public LocalDate dataPagamento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processo_boleto", joinColumns = @JoinColumn(name = "processo_id"),
            indexes = @Index(name = "ix_processo_boleto_emissor", columnList = "id_no_emissor"))
    @OrderColumn(name = "ordem")
    public List<BoletoJpa> boletos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processo_aprovacao", joinColumns = @JoinColumn(name = "processo_id"))
    @OrderColumn(name = "ordem")
    public List<AprovacaoJpa> aprovacoes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "processo_tentativa", joinColumns = @JoinColumn(name = "processo_id"),
            indexes = @Index(name = "ux_tentativa_transacao", columnList = "id_transacao", unique = true))
    @OrderColumn(name = "ordem")
    public List<TentativaJpa> tentativas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public StatusProcesso status;

    @Column(name = "motivo_rejeicao")
    public String motivoRejeicao;

    @Column(name = "motivo_cancelamento")
    public String motivoCancelamento;

    @Column(name = "motivo_analise_codigo", length = 60)
    public String motivoAnaliseCodigo;

    @Column(name = "motivo_analise_mensagem")
    public String motivoAnaliseMensagem;

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

    @Embeddable
    public static class AprovacaoJpa {
        @Column(nullable = false, length = 8)
        public String aprovador;
        @Column(nullable = false)
        public Instant momento;
    }

    @Embeddable
    public static class TentativaJpa {
        @Column(nullable = false)
        public int numero;
        @Column(name = "id_transacao", nullable = false, length = 50)
        public String idTransacao;
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        public StatusTentativa status;
        @Column(name = "enviada_em", nullable = false)
        public Instant enviadaEm;
        @Column(name = "valor_liquidado", precision = 15, scale = 2)
        public BigDecimal valorLiquidado;
        @Column(name = "codigo_autenticacao", length = 60)
        public String codigoAutenticacao;
        @Column(name = "motivo_recusa")
        public String motivoRecusa;
        @Column(name = "concluida_em")
        public Instant concluidaEm;
    }
}
