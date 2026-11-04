package com.example.financiamento.application.financiamento.comando;

import com.example.financiamento.domain.entity.Financiamento;

/**
 * Padrão COMMAND: cada TRANSIÇÃO pedida pelo usuário é um objeto. Todas têm o mesmo roteiro
 * (travar, executar, salvar, efeito externo após o commit), que fica num só invocador:
 * {@code ExecutarComandoFinanciamentoUC}. Novo comando = nova classe aqui, sem novo caso de uso.
 *
 * <p>O comando só ORQUESTRA: a regra continua no agregado ({@code financiamento.submeter(...)}).
 */
public sealed interface ComandoFinanciamento
        permits SubmeterFinanciamento, ContratarFinanciamento, ReenviarRegistro, CancelarFinanciamento {

    /** Muda o agregado (dentro da transação, com o financiamento travado). */
    void executar(Financiamento financiamento, ContextoComando contexto);

    /**
     * Efeito fora do nosso banco de dados (ex.: enviar a garantia para registro). Roda só DEPOIS DO COMMIT:
     * se a transação falhar, nada sai; e o sistema externo nunca responde sobre algo que ainda não gravamos.
     * Uma falha aqui não desfaz o financiamento: em produção, use Outbox ou um job de reenvio. Padrão: nenhum.
     */
    default void depoisDoCommit(Financiamento financiamento, ContextoComando contexto) {
    }
}
