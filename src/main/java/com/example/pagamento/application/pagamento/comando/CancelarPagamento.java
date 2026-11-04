package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.valueobject.Motivo;

/** Cancela (o estado atual diz se pode). O comando carrega os próprios parâmetros. */
public record CancelarPagamento(String motivo) implements ComandoPagamento {

    @Override
    public void executar(Pagamento pagamento, ContextoComando contexto) {
        pagamento.cancelar(new Motivo(motivo));
    }
}
