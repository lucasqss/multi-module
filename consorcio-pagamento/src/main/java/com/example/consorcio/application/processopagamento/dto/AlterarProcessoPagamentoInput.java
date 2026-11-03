package com.example.consorcio.application.processopagamento.dto;

import com.example.consorcio.domain.enums.FormaPagamento;
import com.example.consorcio.domain.valueobject.DadosPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Alteração substitui todos os dados editáveis (semântica de PUT). A cota não é editável. */
public record AlterarProcessoPagamentoInput(
        UUID id,
        BigDecimal valor,
        FormaPagamento formaPagamento,
        LocalDate dataVencimento,
        String agenciaDebito,
        String contaDebito) {

    public DadosPagamento dadosPagamento() {
        return DadosPagamento.de(valor, formaPagamento, dataVencimento, agenciaDebito, contaDebito);
    }
}
