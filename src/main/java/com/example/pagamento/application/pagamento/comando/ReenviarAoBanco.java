package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.domain.entity.Pagamento;

/** RECUSADO -> EM_PROCESSAMENTO, com nova transação, e envia de novo ao banco. */
public record ReenviarAoBanco() implements ComandoPagamento {

    @Override
    public void executar(Pagamento pagamento, ContextoComando contexto) {
        pagamento.reenviarAoBanco();
    }

    @Override
    public void depoisDeSalvar(Pagamento pagamento, ContextoComando contexto) {
        contexto.banco().enviar(pagamento);
    }
}
