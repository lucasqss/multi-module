package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "EXEMPLAR")
public class ExemplarEntity {

    @Id
    private UUID id;

    @Column(name = "LIVRO_ID", nullable = false)
    private UUID livroId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIVRO_ID", insertable = false, updatable = false)
    private LivroEntity livro;

    @Column(nullable = false, unique = true, length = 30)
    private String tombo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoExemplar situacao;

    protected ExemplarEntity() {
    }

    public ExemplarEntity(UUID id, UUID livroId, String tombo, SituacaoExemplar situacao) {
        this.id = id;
        this.livroId = livroId;
        this.tombo = tombo;
        this.situacao = situacao;
    }

    public UUID getId() { return id; }
    public UUID getLivroId() { return livroId; }
    public LivroEntity getLivro() { return livro; }
    public String getTombo() { return tombo; }
    public SituacaoExemplar getSituacao() { return situacao; }
}
