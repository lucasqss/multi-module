package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Filha de {@link ProcessoEntity}: gravá-la insere a linha em PROCESSO e em PROCESSO_IMOBILIARIO. */
@Entity
@Table(name = "PROCESSO_IMOBILIARIO")
@DiscriminatorValue("IMOBILIARIO")
@PrimaryKeyJoinColumn(name = "PROCESSO_ID")
public class ProcessoImobiliarioEntity extends ProcessoEntity {

    @Column(nullable = false, length = 20)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_IMOVEL", nullable = false, length = 20)
    private TipoImovel tipoImovel;

    @Column(name = "VALOR_AVALIACAO", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorAvaliacao;

    @Column(nullable = false, length = 2)
    private String uf;

    protected ProcessoImobiliarioEntity() {
    }

    public ProcessoImobiliarioEntity(UUID id, String numero, String proponenteNome, String proponenteCpf,
                                     BigDecimal valor, SituacaoProcesso situacao, LocalDate dataAbertura,
                                     String matricula, TipoImovel tipoImovel, BigDecimal valorAvaliacao, String uf) {
        super(id, numero, proponenteNome, proponenteCpf, valor, situacao, dataAbertura);
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
