package br.com.consorcio.utilizacao.dominio.nucleo.partes.modelo;

import br.com.consorcio.utilizacao.dominio.nucleo.partes.api.Papel;
import jakarta.persistence.*;

/** Uma pessoa num papel dentro de um processo. O processo é referenciado por id: outro módulo. */
@Entity
@Table(name = "pes_participacao")
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    private Papel papel;

    protected Participacao() {
    }

    public Participacao(Long processoId, Pessoa pessoa, Papel papel) {
        this.processoId = processoId;
        this.pessoa = pessoa;
        this.papel = papel;
    }

    public Long id() { return id; }

    public Long processoId() { return processoId; }

    public Pessoa pessoa() { return pessoa; }

    public Papel papel() { return papel; }
}
