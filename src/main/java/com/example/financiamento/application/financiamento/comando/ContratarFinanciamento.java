package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.domain.entity.Financiamento;

/** APROVADO -> EM_REGISTRO e envia a garantia ao órgão de registro do tipo (cartório ou trânsito). */
public record ContratarFinanciamento() implements ComandoFinanciamento {

    @Override
    public void executar(Financiamento financiamento, ContextoComando contexto) {
        financiamento.contratar();
    }

    @Override
    public void depoisDoCommit(Financiamento financiamento, ContextoComando contexto) {
        contexto.registro().enviar(financiamento);
    }
}
