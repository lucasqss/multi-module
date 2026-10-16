package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Um autor de um livro, na posição em que aparece na obra (a ordem importa para a referência ABNT). */
@Entity
@Table(name = "LIVRO_AUTOR")
public class AutoriaEntity {

    @Id
    private UUID id;

    @Column(name = "LIVRO_ID", nullable = false)
    private UUID livroId;

    /** Somente leitura: grava-se pelo id; a associação existe para joins nas consultas. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIVRO_ID", insertable = false, updatable = false)
    private LivroEntity livro;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private String nome;

    protected AutoriaEntity() {
    }

    public AutoriaEntity(UUID id, UUID livroId, int ordem, String nome) {
        this.id = id;
        this.livroId = livroId;
        this.ordem = ordem;
        this.nome = nome;
    }

    public UUID getId() { return id; }
    public UUID getLivroId() { return livroId; }
    public LivroEntity getLivro() { return livro; }
    public int getOrdem() { return ordem; }
    public String getNome() { return nome; }
}
