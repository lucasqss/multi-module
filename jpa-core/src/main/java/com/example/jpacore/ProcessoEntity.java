package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade pai de todos os segmentos: dados comuns na tabela PROCESSO. Cada segmento é uma subclasse com a
 * própria tabela (JOINED), ligada pela PK; a coluna SEGMENTO (discriminador) diz qual filha completa a linha.
 */
@Entity
@Table(name = "PROCESSO")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "SEGMENTO", length = 30)
public abstract class ProcessoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    /** O discriminador, só para leitura (listagens): quem grava é o Hibernate, pelo @DiscriminatorValue da filha. */
    @Column(name = "SEGMENTO", insertable = false, updatable = false)
    private String segmento;

    @Column(name = "PROPONENTE_NOME", nullable = false)
    private String proponenteNome;

    @Column(name = "PROPONENTE_CPF", nullable = false, length = 11)
    private String proponenteCpf;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoProcesso situacao;

    /** Preenchido só quando a situação é RECUSADO. */
    @Column(name = "MOTIVO_RECUSA")
    private String motivoRecusa;

    @Column(name = "DATA_ABERTURA", nullable = false)
    private LocalDate dataAbertura;

    protected ProcessoEntity() {
    }

    protected ProcessoEntity(UUID id, String numero, String proponenteNome, String proponenteCpf, BigDecimal valor,
                             SituacaoProcesso situacao, LocalDate dataAbertura) {
        this.id = id;
        this.numero = numero;
        this.proponenteNome = proponenteNome;
        this.proponenteCpf = proponenteCpf;
        this.valor = valor;
        this.situacao = situacao;
        this.dataAbertura = dataAbertura;
    }

    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public String getSegmento() { return segmento; }
    public String getProponenteNome() { return proponenteNome; }
    public String getProponenteCpf() { return proponenteCpf; }
    public BigDecimal getValor() { return valor; }
    public SituacaoProcesso getSituacao() { return situacao; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public LocalDate getDataAbertura() { return dataAbertura; }
}
