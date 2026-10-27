package com.example.processo.core.motor.domain.model;

import com.example.processo.core.motor.domain.exception.RegraVioladaException;

/**
 * CPF válido por construção: 11 dígitos, não todos iguais, com os dois dígitos verificadores corretos. Quem recebe
 * um {@code Cpf} não precisa validar de novo; a validação acontece uma vez, onde o texto entra no domínio.
 */
public record Cpf(String numero) {

    public Cpf {
        if (!valido(numero)) {
            throw new RegraVioladaException("CPF inválido: " + numero);
        }
    }

    private static boolean valido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            if ((soma * 10) % 11 % 10 != cpf.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }

    /** Só os dígitos: é como o CPF aparece em mensagens e é gravado. */
    @Override
    public String toString() {
        return numero;
    }
}
