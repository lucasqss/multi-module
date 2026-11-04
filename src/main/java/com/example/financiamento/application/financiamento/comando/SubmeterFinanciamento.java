package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.domain.entity.Financiamento;

import java.time.LocalDate;

/** Finaliza a edição: EM_EDICAO -> APROVADO (regras de crédito e do tipo validadas pelo agregado). */
public record SubmeterFinanciamento() implements ComandoFinanciamento {

    @Override
    public void executar(Financiamento financiamento, ContextoComando contexto) {
        financiamento.submeter(LocalDate.now(contexto.clock()));
    }
}
