package com.example.financiamento.entrypoint.api.v1.request;

import com.example.financiamento.application.financiamento.dto.AlterarDadosInput;

import java.math.BigDecimal;
import java.util.UUID;

/** Seção "dados gerais". Tudo opcional: o rascunho pode ficar incompleto até a submissão. */
public record AlterarFinanciamentoRequest(ProponenteRequest proponente, BigDecimal valorSolicitado, Integer prazoMeses) {

    public record ProponenteRequest(String nome, String cpf, BigDecimal rendaMensal) {
    }

    public AlterarDadosInput paraInput(UUID id) {
        return new AlterarDadosInput(id,
                proponente == null ? null : proponente.nome(),
                proponente == null ? null : proponente.cpf(),
                proponente == null ? null : proponente.rendaMensal(),
                valorSolicitado, prazoMeses);
    }
}
