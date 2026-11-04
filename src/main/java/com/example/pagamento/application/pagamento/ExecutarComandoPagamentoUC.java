package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.banco.BancoPagamentos;
import com.example.pagamento.application.pagamento.comando.ComandoPagamento;
import com.example.pagamento.application.pagamento.comando.ContextoComando;
import com.example.pagamento.application.pagamento.dto.PagamentoOutput;
import com.example.pagamento.domain.entity.Pagamento;
import com.example.pagamento.domain.repository.PagamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * INVOCADOR do padrão Command: o único caso de uso das TRANSIÇÕES (submeter, enviar, reenviar, cancelar).
 * O roteiro é sempre o mesmo, então fica uma vez só; o que muda é o {@link ComandoPagamento}.
 *
 * <p>Usa lock PESSIMISTA: duas transições simultâneas no mesmo pagamento não se atropelam;
 * a segunda espera ou recebe 409 RECURSO_BLOQUEADO. O efeito externo do comando só roda depois do commit.
 */
@ApplicationScoped
public class ExecutarComandoPagamentoUC {

    private final PagamentoRepository repository;
    private final TransactionSynchronizationRegistry transacao;
    private final ContextoComando contexto;

    public ExecutarComandoPagamentoUC(PagamentoRepository repository, TransactionSynchronizationRegistry transacao,
                                      Clock clock, BancoPagamentos banco) {
        this.repository = repository;
        this.transacao = transacao;
        this.contexto = new ContextoComando(clock, banco);
    }

    @Transactional
    public PagamentoOutput executar(UUID id, ComandoPagamento comando) {
        var pagamento = repository.obterParaAlteracao(id);

        comando.executar(pagamento, contexto);

        repository.salvar(pagamento);
        aposCommit(comando, pagamento);
        return PagamentoOutput.de(pagamento);
    }

    /** Rollback = o efeito externo nem acontece. */
    private void aposCommit(ComandoPagamento comando, Pagamento pagamento) {
        transacao.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    comando.depoisDoCommit(pagamento, contexto);
                }
            }
        });
    }
}
