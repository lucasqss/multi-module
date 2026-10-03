package com.example.biblioteca.command.application;

import com.example.biblioteca.command.domain.Leitor;
import com.example.biblioteca.command.domain.Leitores;
import com.example.biblioteca.command.domain.RegraVioladaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class LeitorService {

    private final Leitores leitores;

    public LeitorService(Leitores leitores) {
        this.leitores = leitores;
    }

    @Transactional
    public UUID cadastrar(String nome, String email) {
        if (email != null && leitores.existeEmail(email)) {
            throw new RegraVioladaException("Já existe leitor com e-mail " + email);
        }
        var leitor = Leitor.cadastrar(nome, email);
        leitores.salvar(leitor);
        return leitor.id();
    }
}
