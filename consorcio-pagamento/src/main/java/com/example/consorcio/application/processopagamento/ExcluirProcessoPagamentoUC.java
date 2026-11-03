package com.example.consorcio.application.processopagamento;

import com.example.consorcio.domain.repository.ProcessoPagamentoRepository;
import com.example.consorcio.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/**
 * DELETE (exclusão física). Só é permitida enquanto o processo nunca saiu do sistema (ABERTO);
 * depois disso o caminho é CANCELAR, que preserva o histórico. Quem decide é o estado.
 */
@ApplicationScoped
public class ExcluirProcessoPagamentoUC {

    private final ProcessoPagamentoRepository repository;

    public ExcluirProcessoPagamentoUC(ProcessoPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void executar(UUID id) {
        var processo = repository.obter(new ProcessoPagamentoId(id));
        processo.validarExclusao();
        repository.remover(processo.getId());
    }
}
