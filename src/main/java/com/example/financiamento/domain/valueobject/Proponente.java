package com.example.financiamento.domain.valueobject;

import com.example.financiamento.domain.exception.ProponenteInvalidoException;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/** Quem pede o financiamento. A renda mensal define a capacidade de pagamento. */
@Embeddable
public record Proponente(String nome, Cpf cpf, Dinheiro rendaMensal) {

    public Proponente {
        if (nome == null || nome.isBlank()) {
            throw new ProponenteInvalidoException("nome é obrigatório");
        }
        Objects.requireNonNull(cpf, "cpf é obrigatório");
        if (rendaMensal == null || !rendaMensal.ehPositivo()) {
            throw new ProponenteInvalidoException("renda mensal deve ser positiva");
        }
        nome = nome.strip();
    }
}
