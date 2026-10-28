package com.example.processo.modelo;

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
 * Processo de qualquer modalidade: os dados comuns ficam na tabela PROCESSO. Cada modalidade é uma subclasse com a
 * própria tabela (herança JOINED, ligada pela PK); a coluna SEGMENTO (discriminador) diz qual filha completa a linha.
 *
 * <p>É também o Context do padrão State: guarda a {@link Situacao} e delega cada evento a ela, que decide se a
 * transição vale. Quem grava a nova situação é o {@code ProcessoService}, com compare-and-set.
 *
 * <p>Nasce como pedido de abertura (sem id, número nem data), passa pelas regras e só então é aberto e gravado.
 */
@Entity
@Table(name = "PROCESSO")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "SEGMENTO", length = 30)
public abstract class Processo {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero;

    /** O discriminador. Quem grava é o Hibernate, pelo @DiscriminatorValue da filha; aqui é só para leitura. */
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
    private Situacao situacao;

    /** Preenchido só quando a situação é RECUSADO. */
    @Column(name = "MOTIVO_RECUSA")
    private String motivoRecusa;

    @Column(name = "DATA_ABERTURA", nullable = false)
    private LocalDate dataAbertura;

    protected Processo() {
    }

    /**
     * Pedido de abertura. CPF e valor chegam já válidos (value objects) ou nulos; campos ausentes seguem nulos para as
     * regras recusarem com a mensagem certa.
     */
    protected Processo(String segmento, String proponenteNome, Cpf cpf, Valor valor) {
        this.segmento = segmento;
        this.proponenteNome = proponenteNome;
        this.proponenteCpf = cpf == null ? null : cpf.numero();
        this.valor = valor == null ? null : valor.quantia();
    }

    /** Depois das regras: ganha id, número e data, e nasce ABERTO. */
    public void abrir(String numero, LocalDate hoje) {
        this.id = UUID.randomUUID();
        this.numero = numero;
        this.dataAbertura = hoje;
        this.situacao = Situacao.ABERTO;
    }

    public void iniciarProcessamento() {
        situacao = situacao.iniciarProcessamento();
    }

    public void concluir() {
        situacao = situacao.concluir();
    }

    public void recusar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Recusa exige motivo");
        }
        situacao = situacao.recusar();
        motivoRecusa = motivo;
    }

    public void cancelar() {
        situacao = situacao.cancelar();
    }

    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public String getSegmento() { return segmento; }
    public String getProponenteNome() { return proponenteNome; }
    public String getProponenteCpf() { return proponenteCpf; }
    public BigDecimal getValor() { return valor; }
    public Situacao getSituacao() { return situacao; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public LocalDate getDataAbertura() { return dataAbertura; }
}
