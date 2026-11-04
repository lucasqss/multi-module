package com.example.financiamento.domain.specification.financiamento;

import com.example.financiamento.domain.valueobject.Bem;
import com.example.financiamento.domain.valueobject.Dinheiro;
import com.example.financiamento.domain.valueobject.DocumentosDoFinanciamento;
import com.example.financiamento.domain.valueobject.Proponente;
import com.example.financiamento.domain.valueobject.TipoFinanciamento;

import java.time.LocalDate;

/**
 * CANDIDATO das regras de SUBMISSÃO: o financiamento como está ao fim da edição + "hoje".
 * Proponente, condições e garantia podem faltar (o rascunho é incompleto): as primeiras regras do
 * catálogo exigem que existam, e as seguintes podem contar com isso (a validação para na primeira violada).
 */
public record SubmissaoFinanciamento(TipoFinanciamento tipo, Proponente proponente, Dinheiro valorSolicitado,
                                     Integer prazoMeses, Bem bem, DocumentosDoFinanciamento documentos, LocalDate hoje) {

    /** Parcela SIMPLIFICADA (exemplo, sem juros): valor solicitado / prazo. */
    public Dinheiro parcela() {
        return valorSolicitado.dividido(prazoMeses);
    }
}
