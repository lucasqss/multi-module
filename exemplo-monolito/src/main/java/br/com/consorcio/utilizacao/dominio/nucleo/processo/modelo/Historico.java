package br.com.consorcio.utilizacao.dominio.nucleo.processo.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Append-only. Toda transição grava aqui, na mesma transação. */
@Entity
@Table(name = "prc_historico")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processo_id")
    private Long processoId;

    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    private String de;

    private String para;

    private String motivo;

    private String ator;

    private LocalDateTime em;

    protected Historico() {
    }

    public Historico(Long processoId, String entidade, Long entidadeId, Object de, Object para, String motivo, String ator) {
        this.processoId = processoId;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.de = de == null ? null : de.toString();
        this.para = para.toString();
        this.motivo = motivo;
        this.ator = ator;
        this.em = LocalDateTime.now();
    }
}
