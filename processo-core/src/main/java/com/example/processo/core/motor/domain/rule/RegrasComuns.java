package com.example.processo.core.motor.domain.rule;

import com.example.processo.core.motor.domain.model.DadosDoSegmento;
import com.example.processo.core.motor.domain.model.Solicitacao;

/**
 * Regras que valem para todos os segmentos. O motor as aplica antes das regras do segmento. CPF e valor não estão
 * aqui: {@link com.example.processo.core.motor.domain.model.Cpf} e
 * {@link com.example.processo.core.motor.domain.model.Valor} já nascem válidos.
 */
public final class RegrasComuns {

    private RegrasComuns() {
    }

    public static <D extends DadosDoSegmento> Especificacao<Solicitacao<D>> todas() {
        Especificacao<Solicitacao<D>> nome = Especificacao.exige(
                s -> s.proponente() != null && s.proponente().nome() != null && !s.proponente().nome().isBlank(),
                "Nome do proponente é obrigatório");
        Especificacao<Solicitacao<D>> valor = Especificacao.exige(s -> s.valor() != null,
                "Valor do processo deve ser maior que zero");
        return nome.e(valor);
    }
}
