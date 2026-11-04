package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.LinhaDigitavel;
import com.example.boleto.domain.valueobject.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Aviso de pagamento vindo do sistema externo, já traduzido do formato dele pelo entrypoint
 * (centavos -> reais, data/hora com fuso -> instante).
 */
public record ReceberPagamentoInput(String linhaDigitavel, String idTransacao, BigDecimal valorPago,
                                    Instant momentoPagamento) {

    public LinhaDigitavel linha() {
        return new LinhaDigitavel(linhaDigitavel);
    }

    /** A data do pagamento é a data no fuso do NEGÓCIO (não no do sistema externo nem no do servidor). */
    public Pagamento pagamento(ZoneId fusoDoNegocio) {
        return new Pagamento(idTransacao, new Dinheiro(valorPago), LocalDate.ofInstant(momentoPagamento, fusoDoNegocio));
    }
}
