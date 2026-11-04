package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.valueobject.Motivo;

/** Cancela (o status atual diz se pode). O comando carrega os próprios parâmetros. */
public record CancelarFinanciamento(String motivo) implements ComandoFinanciamento {

    @Override
    public void executar(Financiamento financiamento, ContextoComando contexto) {
        financiamento.cancelar(new Motivo(motivo));
    }
}
