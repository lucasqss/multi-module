package br.com.consorcio.utilizacao.dominio.garantia.modelo;

import jakarta.persistence.*;

/** Fica no módulo garantia (e não em pes_pessoa) para o módulo continuar extraível. */
@Entity
@Table(name = "gar_fiador")
public class Fiador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "garantia_id")
    private Garantia garantia;

    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    private String nome;

    protected Fiador() {
    }

    Fiador(Garantia garantia, String cpfCnpj, String nome) {
        this.garantia = garantia;
        this.cpfCnpj = cpfCnpj;
        this.nome = nome;
    }
}
