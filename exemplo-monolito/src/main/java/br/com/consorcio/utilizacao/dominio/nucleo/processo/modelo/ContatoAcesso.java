package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import jakarta.persistence.*;

/** Quem dá acesso ao vistoriador. Dado de terceiro: descartar ao encerrar o processo. */
@Entity
@Table(name = "prc_contato_acesso")
public class ContatoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "objeto_id")
    private Objeto objeto;

    private String nome;

    private String email;

    private String telefone;

    protected ContatoAcesso() {
    }

    ContatoAcesso(Objeto objeto, String nome, String email, String telefone) {
        this.objeto = objeto;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }
}
