package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagamento;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO avaliado pelas specifications de recebimento do pagamento:
 * o que o boleto cobra versus o que o banco informou que foi pago.
 * Montado pela própria entidade {@code Boleto}, que é quem conhece os dois lados.
 */
public record RecebimentoPagamento(
        Dinheiro valorBoleto,
        LocalDate dataVencimento,
        Pagamento pagamento,
        LocalDate hoje) {

    public RecebimentoPagamento {
        Objects.requireNonNull(valorBoleto);
        Objects.requireNonNull(dataVencimento);
        Objects.requireNonNull(pagamento);
        Objects.requireNonNull(hoje);
    }
}
