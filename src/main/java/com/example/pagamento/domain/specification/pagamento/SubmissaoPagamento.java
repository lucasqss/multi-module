package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.valueobject.BoletosDoPagamento;
import com.example.pagamento.domain.valueobject.ContaDebito;
import com.example.pagamento.domain.valueobject.Modalidade;

import java.time.LocalDate;

/**
 * CANDIDATO das regras de SUBMISSÃO (completude): o pagamento como está ao fim da edição + "hoje".
 * Conta e data podem faltar (o rascunho é incompleto): as primeiras regras do catálogo exigem que existam,
 * e as seguintes podem contar com isso (a validação para na primeira violada).
 */
public record SubmissaoPagamento(Modalidade modalidade, BoletosDoPagamento boletos, ContaDebito contaDebito,
                                 LocalDate dataPagamento, LocalDate hoje) {
}
