package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "EMPRESTIMO")
public class EmprestimoEntity {

    @Id
    private UUID id;

    @Column(name = "LEITOR_ID", nullable = false)
    private UUID leitorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEITOR_ID", insertable = false, updatable = false)
    private LeitorEntity leitor;

    @Column(name = "EXEMPLAR_ID", nullable = false)
    private UUID exemplarId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXEMPLAR_ID", insertable = false, updatable = false)
    private ExemplarEntity exemplar;

    @Column(name = "DATA_EMPRESTIMO", nullable = false)
    private LocalDate data;

    @Column(name = "DATA_PREVISTA", nullable = false)
    private LocalDate dataPrevista;

    @Column(nullable = false)
    private int renovacoes;

    @Column(name = "DATA_DEVOLUCAO")
    private LocalDate dataDevolucao;

    protected EmprestimoEntity() {
    }

    public EmprestimoEntity(UUID id, UUID leitorId, UUID exemplarId, LocalDate data, LocalDate dataPrevista,
                            int renovacoes, LocalDate dataDevolucao) {
        this.id = id;
        this.leitorId = leitorId;
        this.exemplarId = exemplarId;
        this.data = data;
        this.dataPrevista = dataPrevista;
        this.renovacoes = renovacoes;
        this.dataDevolucao = dataDevolucao;
    }

    public UUID getId() { return id; }
    public UUID getLeitorId() { return leitorId; }
    public LeitorEntity getLeitor() { return leitor; }
    public UUID getExemplarId() { return exemplarId; }
    public ExemplarEntity getExemplar() { return exemplar; }
    public LocalDate getData() { return data; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public int getRenovacoes() { return renovacoes; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
}
