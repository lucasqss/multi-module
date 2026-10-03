package com.example.jpacore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "LEITOR")
public class LeitorEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "SUSPENSO_ATE")
    private LocalDate suspensoAte;

    protected LeitorEntity() {
    }

    public LeitorEntity(UUID id, String nome, String email, LocalDate suspensoAte) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.suspensoAte = suspensoAte;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public LocalDate getSuspensoAte() { return suspensoAte; }
}
