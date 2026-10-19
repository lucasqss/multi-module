package com.example.processo.motor.domain;

import java.util.Optional;

/** Regras que valem para todos os segmentos. O motor as aplica antes das regras do segmento. */
public final class RegrasComuns {

    private RegrasComuns() {
    }

    public static <D extends DadosDoSegmento> Especificacao<Solicitacao<D>> todas() {
        Especificacao<Solicitacao<D>> nome = Especificacao.exige(
                s -> s.proponente() != null && s.proponente().nome() != null && !s.proponente().nome().isBlank(),
                "Nome do proponente é obrigatório");
        Especificacao<Solicitacao<D>> cpf = s -> cpfValido(s.proponente().cpf())
                ? Optional.empty()
                : Optional.of("CPF inválido: " + s.proponente().cpf());
        Especificacao<Solicitacao<D>> valor = Especificacao.exige(
                s -> s.valor() != null && s.valor().signum() > 0, "Valor do processo deve ser maior que zero");
        return nome.e(cpf).e(valor);
    }

    /** 11 dígitos, não todos iguais, com os dois dígitos verificadores corretos. */
    public static boolean cpfValido(String cpf) {
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
}
