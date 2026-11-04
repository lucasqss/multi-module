package com.example.pagamento.domain.specification.processo;

import com.example.pagamento.domain.valueobject.Dinheiro;
import com.example.pagamento.domain.valueobject.RetornoBanco;

/** CANDIDATO da CONFORMIDADE do retorno do banco (um fato): o que o processo soma × o que o banco liquidou. */
public record RetornoPagamento(Dinheiro valorTotal, RetornoBanco retorno) {
}
