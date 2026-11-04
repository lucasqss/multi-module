package com.example.pagamento.application.pagamento.comando;

import com.example.pagamento.domain.entity.Pagamento;

/**
 * Padrão COMMAND: cada TRANSIÇÃO pedida pelo usuário é um objeto. Todas têm o mesmo roteiro
 * (travar, executar, salvar, efeito externo), que fica num só invocador:
 * {@code ExecutarComandoPagamentoUC}. Novo comando = nova classe aqui, sem novo caso de uso.
 *
 * <p>O comando só ORQUESTRA: a regra continua no agregado ({@code pagamento.submeter(...)}).
 */
public sealed interface ComandoPagamento permits SubmeterPagamento, EnviarAoBanco, ReenviarAoBanco, CancelarPagamento {

    /** Muda o agregado (dentro da transação, com o pagamento travado). */
    void executar(Pagamento pagamento, ContextoComando contexto);

    /** Efeito fora do nosso banco de dados, depois de salvar (ex.: enviar ao banco). Padrão: nenhum. */
    default void depoisDeSalvar(Pagamento pagamento, ContextoComando contexto) {
    }
}
