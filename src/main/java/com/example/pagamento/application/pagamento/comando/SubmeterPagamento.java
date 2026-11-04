package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.domain.entity.Pagamento;

import java.time.LocalDate;

/** Finaliza a edição: EM_EDICAO -> AGENDADO (regras da modalidade validadas pelo agregado). */
public record SubmeterPagamento() implements ComandoPagamento {

    @Override
    public void executar(Pagamento pagamento, ContextoComando contexto) {
        pagamento.submeter(LocalDate.now(contexto.clock()));
    }
}
