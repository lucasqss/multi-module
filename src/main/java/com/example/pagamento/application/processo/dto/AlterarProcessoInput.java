package com.example.pagamento.application.processo.dto;

import com.example.pagamento.domain.valueobject.ContaDebito;

import java.time.LocalDate;
import java.util.UUID;

/** Seção "dados gerais". Campos nulos são permitidos: o rascunho pode estar incompleto. */
public record AlterarProcessoInput(UUID id, String agenciaDebito, String contaDebito, LocalDate dataPagamento) {

    public ContaDebito conta() {
        return agenciaDebito == null && contaDebito == null ? null : new ContaDebito(agenciaDebito, contaDebito);
    }
}
