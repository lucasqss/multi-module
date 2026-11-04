package com.example.pagamento.entrypoint.api.v1.request;

import com.example.pagamento.application.processo.dto.AlterarProcessoInput;

import java.time.LocalDate;
import java.util.UUID;

/** Seção "dados gerais". Tudo opcional: o rascunho pode ficar incompleto até a submissão. */
public record AlterarProcessoRequest(ContaRequest contaDebito, LocalDate dataPagamento) {

    public record ContaRequest(String agencia, String conta) {
    }

    public AlterarProcessoInput paraInput(UUID id) {
        return new AlterarProcessoInput(id,
                contaDebito == null ? null : contaDebito.agencia(),
                contaDebito == null ? null : contaDebito.conta(),
                dataPagamento);
    }
}
