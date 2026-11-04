package com.example.boleto.domain.valueobject;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Pagamento informado pelo SISTEMA EXTERNO de pagamentos: quanto foi pago, quando, e o id da transação
 * lá. O id permite reconhecer um aviso repetido (webhooks são entregues "pelo menos uma vez").
 */
public record Pagamento(String idTransacao, Dinheiro valorPago, LocalDate data) {

    public Pagamento {
        if (idTransacao == null || idTransacao.isBlank()) {
            throw new IllegalArgumentException("id da transação é obrigatório");
        }
        Objects.requireNonNull(valorPago, "valor pago é obrigatório");
        Objects.requireNonNull(data, "data do pagamento é obrigatória");
    }

    public boolean mesmaTransacao(Pagamento outro) {
        return idTransacao.equals(outro.idTransacao);
    }
}
