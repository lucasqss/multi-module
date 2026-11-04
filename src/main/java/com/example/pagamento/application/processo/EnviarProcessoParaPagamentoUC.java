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
 * Envia o processo aprovado ao BANCO (sistema externo). Na vida real, disparado por um job na data de
 * pagamento. APROVADO -> EM_PROCESSAMENTO, com uma nova tentativa.
 */
@ApplicationScoped
public class EnviarProcessoParaPagamentoUC {

    private final ProcessoPagamentoRepository repository;
    private final BancoPagamentos banco;
    private final Clock clock;

    public EnviarProcessoParaPagamentoUC(ProcessoPagamentoRepository repository, BancoPagamentos banco, Clock clock) {
        this.repository = repository;
        this.banco = banco;
        this.clock = clock;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.enviarParaPagamento(clock.instant());
        repository.salvar(processo);

        // integração de SAÍDA só depois de salvar (estado decidiu, concorrência verificada).
        // Risco restante ("enviou, mas o commit falhou"): em produção, padrão Outbox.
        banco.enviar(processo, processo.getTentativaAtual().orElseThrow());
        return ProcessoPagamentoOutput.de(processo);
    }
}
