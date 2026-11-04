package com.example.boleto.domain.specification.boleto;

import com.example.boleto.domain.state.StatusBoleto;
import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagamento;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CANDIDATO das regras de CONFORMIDADE do pagamento: o que o boleto cobra (e se ainda está em aberto)
 * versus o que o sistema de pagamentos informou. Montado pela própria entidade {@code Boleto}.
 */
public record RecebimentoPagamento(
        Dinheiro valorBoleto,
        LocalDate dataVencimento,
        StatusBoleto statusBoleto,
        boolean boletoEmAberto,
        Pagamento pagamento,
        LocalDate hoje) {

    public RecebimentoPagamento {
        Objects.requireNonNull(valorBoleto);
        Objects.requireNonNull(dataVencimento);
        Objects.requireNonNull(statusBoleto);
        Objects.requireNonNull(pagamento);
        Objects.requireNonNull(hoje);
    }
}
