package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.banco.BancoPagamentos;
import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Depois de uma recusa do banco (saldo, conta...), tenta de novo com uma nova tentativa. FALHOU ->
 * EM_PROCESSAMENTO.
 */
@ApplicationScoped
public class RetentarPagamentoUC {

    private final ProcessoPagamentoRepository repository;
    private final BancoPagamentos banco;
    private final Clock clock;

    public RetentarPagamentoUC(ProcessoPagamentoRepository repository, BancoPagamentos banco, Clock clock) {
        this.repository = repository;
        this.banco = banco;
        this.clock = clock;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.retentarPagamento(clock.instant());
        repository.salvar(processo);

        // integração de SAÍDA só depois de salvar (estado decidiu, concorrência verificada).
        // Risco restante ("enviou, mas o commit falhou"): em produção, padrão Outbox.
        banco.enviar(processo, processo.getTentativaAtual().orElseThrow());
        return ProcessoPagamentoOutput.de(processo);
    }
}
