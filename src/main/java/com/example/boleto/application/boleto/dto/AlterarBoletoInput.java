package com.example.boleto.application.boleto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Só valor e vencimento são alteráveis; o pagador de um boleto não muda. */
public record AlterarBoletoInput(UUID id, BigDecimal valor, LocalDate dataVencimento) {
}
