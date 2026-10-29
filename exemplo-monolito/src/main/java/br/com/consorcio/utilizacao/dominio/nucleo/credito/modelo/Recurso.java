package br.com.consorcio.utilizacao.dominio.nucleo.credito.modelo;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Dinheiro que não é carta: recursos próprios, FGTS, sinal. */
@Entity
@Table(name = "cre_recurso")
public class Recurso {

    public enum Tipo { RECURSOS_PROPRIOS, FGTS, SINAL }

    public enum Liquidacao { DIRETO_AO_VENDEDOR, VIA_ADMINISTRADORA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private Liquidacao liquidacao;

    /** DECLARADO, COMPROVADO, CANCELADO */
    private String situacao = "DECLARADO";

    protected Recurso() {
    }

    public Recurso(Long processoId, Tipo tipo, BigDecimal valor, Liquidacao liquidacao) {
        this.processoId = processoId;
        this.tipo = tipo;
        this.valor = valor;
        this.liquidacao = liquidacao;
    }

    public void comprovar() {
        situacao = "COMPROVADO";
    }

    public Long id() { return id; }

    public BigDecimal valor() { return valor; }

    public String situacao() { return situacao; }
}
