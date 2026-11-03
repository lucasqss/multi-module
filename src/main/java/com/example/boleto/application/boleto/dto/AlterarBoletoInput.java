package com.example.boleto.application.boleto.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Seção "dados gerais": só o vencimento é editável (o pagador de um boleto não muda). */
public record AlterarBoletoInput(UUID id, LocalDate dataVencimento) {
}
