package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.application.processo.dto.ProcessoPagamentoOutput;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * Reabre um processo rejeitado para correção (as aprovações anteriores deixam de valer). REJEITADO ->
 * EM_EDICAO.
 */
@ApplicationScoped
public class ReabrirProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ReabrirProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProcessoPagamentoOutput executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.reabrir();
        repository.salvar(processo);
        return ProcessoPagamentoOutput.de(processo);
    }
}
