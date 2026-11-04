package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.domain.entity.Financiamento;

/** REGISTRO_RECUSADO -> EM_REGISTRO, com novo protocolo, e envia de novo ao órgão. */
public record ReenviarRegistro() implements ComandoFinanciamento {

    @Override
    public void executar(Financiamento financiamento, ContextoComando contexto) {
        financiamento.reenviarRegistro();
    }

    @Override
    public void depoisDoCommit(Financiamento financiamento, ContextoComando contexto) {
        contexto.registro().enviar(financiamento);
    }
}
