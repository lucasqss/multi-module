package br.com.consorcio.utilizacao.dominio.nucleo.partes.modelo;

import jakarta.persistence.*;

/** Cadastro mínimo, único por CPF/CNPJ. O papel no processo fica em {@link Participacao}. */
@Entity
@Table(name = "pes_pessoa")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    private String nome;

    protected Pessoa() {
    }

    public Pessoa(String tipo, String cpfCnpj, String nome) {
        this.tipo = tipo;
        this.cpfCnpj = cpfCnpj;
        this.nome = nome;
    }

    public Long id() { return id; }

    public String tipo() { return tipo; }

    public String cpfCnpj() { return cpfCnpj; }

    public String nome() { return nome; }
}
