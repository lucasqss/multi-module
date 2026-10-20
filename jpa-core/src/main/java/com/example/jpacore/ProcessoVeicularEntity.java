package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Filha de {@link ProcessoEntity}: gravá-la insere a linha em PROCESSO e em PROCESSO_VEICULAR. */
@Entity
@Table(name = "PROCESSO_VEICULAR")
@DiscriminatorValue("VEICULAR")
@PrimaryKeyJoinColumn(name = "PROCESSO_ID")
public class ProcessoVeicularEntity extends ProcessoEntity {

    @Column(nullable = false, length = 7)
    private String placa;

    @Column(name = "ANO_FABRICACAO", nullable = false)
    private int anoFabricacao;

    @Column(name = "VALOR_FIPE", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFipe;

    protected ProcessoVeicularEntity() {
    }

    public ProcessoVeicularEntity(UUID id, String numero, String proponenteNome, String proponenteCpf,
                                  BigDecimal valor, SituacaoProcesso situacao, LocalDate dataAbertura,
                                  String placa, int anoFabricacao, BigDecimal valorFipe) {
        super(id, numero, proponenteNome, proponenteCpf, valor, situacao, dataAbertura);
        this.placa = placa;
        this.anoFabricacao = anoFabricacao;
        this.valorFipe = valorFipe;
    }

    public String getPlaca() { return placa; }
    public int getAnoFabricacao() { return anoFabricacao; }
    public BigDecimal getValorFipe() { return valorFipe; }
}
