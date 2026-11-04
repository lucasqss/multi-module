package com.example.financiamento.application.financiamento;

import com.example.financiamento.adapter.registro.RegistroDeGarantias;
import com.example.financiamento.application.financiamento.comando.ComandoFinanciamento;
import com.example.financiamento.application.financiamento.comando.ContextoComando;
import com.example.financiamento.application.financiamento.dto.FinanciamentoOutput;
import com.example.financiamento.domain.entity.Financiamento;
import com.example.financiamento.domain.repository.FinanciamentoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * INVOCADOR do padrão Command: o único caso de uso das TRANSIÇÕES (submeter, contratar, reenviar o registro,
 * cancelar). O roteiro é sempre o mesmo, então fica uma vez só; o que muda é o {@link ComandoFinanciamento}.
 *
 * <p>Usa lock PESSIMISTA: duas transições simultâneas no mesmo financiamento não se atropelam;
 * a segunda espera ou recebe 409 RECURSO_BLOQUEADO. O efeito externo do comando só roda depois do commit.
 */
@ApplicationScoped
public class ExecutarComandoFinanciamentoUC {

    private final FinanciamentoRepository repository;
    private final TransactionSynchronizationRegistry transacao;
    private final ContextoComando contexto;

    public ExecutarComandoFinanciamentoUC(FinanciamentoRepository repository, TransactionSynchronizationRegistry transacao,
                                          Clock clock, RegistroDeGarantias registro) {
        this.repository = repository;
        this.transacao = transacao;
        this.contexto = new ContextoComando(clock, registro);
    }

    @Transactional
    public FinanciamentoOutput executar(UUID id, ComandoFinanciamento comando) {
        var financiamento = repository.obterParaAlteracao(id);

        comando.executar(financiamento, contexto);

        repository.salvar(financiamento);
        aposCommit(comando, financiamento);
        return FinanciamentoOutput.de(financiamento);
    }

    /** Rollback = o efeito externo nem acontece. */
    private void aposCommit(ComandoFinanciamento comando, Financiamento financiamento) {
        transacao.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    comando.depoisDoCommit(financiamento, contexto);
                }
            }
        });
    }
}
