package com.example.modelo.core;

import com.example.modelo.core.interno.Nomes;
import jakarta.enterprise.context.ApplicationScoped;

/** Bean CDI de um módulo-biblioteca: só é descoberto pelo app porque este módulo gera o índice Jandex. */
@ApplicationScoped
public class SaudacaoService {

    private final SaudacaoConfig config;

    public SaudacaoService(SaudacaoConfig config) {
        this.config = config;
    }

    public Saudacao saudar(String nome) {
        var limpo = Nomes.limpar(nome);
        return new Saudacao(limpo, config.prefixo() + ", " + limpo + "!");
    }
}
