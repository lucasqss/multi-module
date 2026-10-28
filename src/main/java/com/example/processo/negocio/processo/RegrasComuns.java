package com.example.processo.negocio.processo;

import com.example.processo.modelo.Processo;

/**
 * Regras que valem para todas as modalidades. O serviço as aplica antes das regras da modalidade. CPF e valor
 * inválidos nem chegam aqui: {@code Cpf} e {@code Valor} já nascem válidos; aqui só se confere que vieram.
 */
public final class RegrasComuns {

    private RegrasComuns() {
    }

    private static final Especificacao<Processo> NOME = Especificacao.exige(
            p -> p.getProponenteNome() != null && !p.getProponenteNome().isBlank(),
            "Nome do proponente é obrigatório");

    private static final Especificacao<Processo> VALOR = Especificacao.exige(
            p -> p.getValor() != null, "Valor do processo deve ser maior que zero");

    public static final Especificacao<Processo> TODAS = NOME.e(VALOR);
}
