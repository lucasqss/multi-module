package com.example.pagamento.entrypoint.api.v1.request;

import com.example.pagamento.application.pagamento.dto.AlterarDadosInput;

import java.time.LocalDate;
import java.util.UUID;

/** Seção "dados gerais". Tudo opcional: o rascunho pode ficar incompleto até a submissão. */
public record AlterarPagamentoRequest(ContaRequest contaDebito, LocalDate dataPagamento) {

    public record ContaRequest(String agencia, String conta) {
    }

    public AlterarDadosInput paraInput(UUID id) {
        return new AlterarDadosInput(id,
                contaDebito == null ? null : contaDebito.agencia(),
                contaDebito == null ? null : contaDebito.conta(),
                dataPagamento);
    }
}
