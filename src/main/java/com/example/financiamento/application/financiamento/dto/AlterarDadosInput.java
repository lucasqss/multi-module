package com.example.financiamento.application.financiamento.dto;

import com.example.financiamento.domain.valueobject.Cpf;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.Proponente;

import java.math.BigDecimal;
import java.util.UUID;

/** Seção "dados gerais". Campos nulos são permitidos: o rascunho pode estar incompleto. */
public record AlterarDadosInput(UUID id, String nomeProponente, String cpfProponente, BigDecimal rendaMensal,
                                BigDecimal valorSolicitado, Integer prazoMeses) {

    public Proponente proponente() {
        if (nomeProponente == null && cpfProponente == null && rendaMensal == null) {
            return null;
        }
        return new Proponente(nomeProponente, new Cpf(cpfProponente), rendaMensal == null ? null : new Dinheiro(rendaMensal));
    }

    public Dinheiro valor() {
        return valorSolicitado == null ? null : new Dinheiro(valorSolicitado);
    }
}
