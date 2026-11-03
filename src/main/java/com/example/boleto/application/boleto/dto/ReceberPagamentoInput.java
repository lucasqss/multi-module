package com.example.boleto.application.boleto.dto;

import com.example.boleto.domain.valueobject.Dinheiro;
import com.example.boleto.domain.valueobject.Pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Aviso de pagamento vindo do banco (na vida real, um webhook ou arquivo de retorno). */
public record ReceberPagamentoInput(UUID id, BigDecimal valorPago, LocalDate dataPagamento) {

    public Pagamento pagamento() {
        return new Pagamento(new Dinheiro(valorPago), dataPagamento);
    }
}
