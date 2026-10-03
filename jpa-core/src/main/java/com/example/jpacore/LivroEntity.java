package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Os autores ficam em {@link AutoriaEntity}: este módulo não mapeia coleções (ver docs/design.md). */
@Entity
@Table(name = "LIVRO")
public class LivroEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false)
    private String titulo;

    private String subtitulo;

    @Column(nullable = false)
    private int edicao;

    private String local;

    private String editora;

    @Column(nullable = false)
    private int ano;

    protected LivroEntity() {
    }

    public LivroEntity(UUID id, String isbn, String titulo, String subtitulo,
                       int edicao, String local, String editora, int ano) {
        this.id = id;
        this.isbn = isbn;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.edicao = edicao;
        this.local = local;
        this.editora = editora;
        this.ano = ano;
    }

    public UUID getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getSubtitulo() { return subtitulo; }
    public int getEdicao() { return edicao; }
    public String getLocal() { return local; }
    public String getEditora() { return editora; }
    public int getAno() { return ano; }
}
