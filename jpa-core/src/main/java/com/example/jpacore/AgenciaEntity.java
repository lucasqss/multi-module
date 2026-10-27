package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

/** Agência: cadastro simples, sem fluxo nem estados. */
@Entity
@Table(name = "AGENCIA")
public class AgenciaEntity {

    @Id
    @Column(length = 4)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 2)
    private String uf;

    /** NUMBER(1) com 0/1: funciona em qualquer Oracle (BOOLEAN nativo só existe a partir do 23). */
    @Convert(converter = NumericBooleanConverter.class)
    @Column(nullable = false)
    private boolean ativa;

    protected AgenciaEntity() {
    }

    public AgenciaEntity(String codigo, String nome, String uf, boolean ativa) {
        this.codigo = codigo;
        this.nome = nome;
        this.uf = uf;
        this.ativa = ativa;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getUf() { return uf; }
    public boolean isAtiva() { return ativa; }
}
