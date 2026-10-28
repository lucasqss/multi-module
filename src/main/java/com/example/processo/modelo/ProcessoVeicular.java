package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Processo da modalidade veicular: gravá-lo insere a linha em PROCESSO e em PROCESSO_VEICULAR. */
@Entity
@Table(name = "PROCESSO_VEICULAR")
@DiscriminatorValue(ProcessoVeicular.SEGMENTO)
@PrimaryKeyJoinColumn(name = "PROCESSO_ID")
public class ProcessoVeicular extends Processo {

    public static final String SEGMENTO = "VEICULAR";

    @Column(nullable = false, length = 7)
    private String placa;

    @Column(name = "ANO_FABRICACAO", nullable = false)
    private Integer anoFabricacao;

    @Column(name = "VALOR_FIPE", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFipe;

    protected ProcessoVeicular() {
    }

    public ProcessoVeicular(String proponenteNome, Cpf cpf, Valor valor,
                            String placa, Integer anoFabricacao, BigDecimal valorFipe) {
        super(SEGMENTO, proponenteNome, cpf, valor);
        this.placa = placa;
        this.anoFabricacao = anoFabricacao;
        this.valorFipe = valorFipe;
    }

    public String getPlaca() { return placa; }
    public Integer getAnoFabricacao() { return anoFabricacao; }
    public BigDecimal getValorFipe() { return valorFipe; }
}
