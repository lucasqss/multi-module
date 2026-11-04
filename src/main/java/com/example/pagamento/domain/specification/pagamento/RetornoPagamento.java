package com.example.pagamento.domain.specification.pagamento;

import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.RetornoBanco;

/** CANDIDATO da CONFORMIDADE do retorno do banco (um fato): o total do pagamento × o que o banco liquidou. */
public record RetornoPagamento(Dinheiro valorTotal, RetornoBanco retorno) {
}
