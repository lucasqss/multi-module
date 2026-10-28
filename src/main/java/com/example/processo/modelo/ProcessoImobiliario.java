package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Processo da modalidade imobiliária: gravá-lo insere a linha em PROCESSO e em PROCESSO_IMOBILIARIO. */
@Entity
@Table(name = "PROCESSO_IMOBILIARIO")
@DiscriminatorValue(ProcessoImobiliario.SEGMENTO)
@PrimaryKeyJoinColumn(name = "PROCESSO_ID")
public class ProcessoImobiliario extends Processo {

    public static final String SEGMENTO = "IMOBILIARIO";

    @Column(nullable = false, length = 20)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_IMOVEL", nullable = false, length = 20)
    private TipoImovel tipoImovel;

    @Column(name = "VALOR_AVALIACAO", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAvaliacao;

    @Column(nullable = false, length = 2)
    private String uf;

    protected ProcessoImobiliario() {
    }

    public ProcessoImobiliario(String proponenteNome, Cpf cpf, Valor valor,
                               String matricula, TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf) {
        super(SEGMENTO, proponenteNome, cpf, valor);
        this.matricula = matricula;
        this.tipoImovel = tipoImovel;
        this.valorAvaliacao = valorAvaliacao;
        this.uf = uf;
    }

    public String getMatricula() { return matricula; }
    public TipoImovel getTipoImovel() { return tipoImovel; }
    public BigDecimal getValorAvaliacao() { return valorAvaliacao; }
    public String getUf() { return uf; }
}
