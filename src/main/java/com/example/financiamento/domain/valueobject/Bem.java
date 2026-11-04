package com.example.financiamento.domain.valueobject;

import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * O bem dado em garantia, como o SISTEMA DE AVALIAÇÃO (externo) o devolveu: identificação (matrícula ou
 * placa), descrição e valor avaliado. {@code anoModelo} só existe para veículo.
 */
@Embeddable
public record Bem(String identificacao, String descricao, Dinheiro valorAvaliado, Integer anoModelo) {

    public Bem {
        Objects.requireNonNull(identificacao);
        Objects.requireNonNull(descricao);
        Objects.requireNonNull(valorAvaliado);
    }
}
