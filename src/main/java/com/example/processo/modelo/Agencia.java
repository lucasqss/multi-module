package com.example.processo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

/**
 * Agência: cadastro simples, sem fluxo nem estados. Válida por construção: inválida dá 422 com o motivo, sem uma
 * camada de regras à parte. O construtor sem argumentos é só do Hibernate (lê o que já foi gravado válido).
 */
@Entity
@Table(name = "AGENCIA")
public class Agencia {

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

    protected Agencia() {
    }

    public Agencia(String codigo, String nome, String uf, boolean ativa) {
        if (codigo == null || !codigo.matches("\\d{4}")) {
            throw new RegraVioladaException("Código da agência deve ter 4 dígitos: " + codigo);
        }
        if (nome == null || nome.isBlank()) {
            throw new RegraVioladaException("Nome da agência é obrigatório");
        }
        if (uf == null || !uf.matches("[A-Z]{2}")) {
            throw new RegraVioladaException("UF inválida: " + uf);
        }
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
