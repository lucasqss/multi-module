package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.domain.entity.Pagamento;

/** AGENDADO -> EM_PROCESSAMENTO e envia ao banco. Na vida real, disparado por um job na data. */
public record EnviarAoBanco() implements ComandoPagamento {

    @Override
    public void executar(Pagamento pagamento, ContextoComando contexto) {
        pagamento.enviarAoBanco();
    }

    /** Em produção, prefira Outbox: gravar a intenção junto com o pagamento e enviar depois do commit. */
    @Override
    public void depoisDeSalvar(Pagamento pagamento, ContextoComando contexto) {
        contexto.banco().enviar(pagamento);
    }
}
