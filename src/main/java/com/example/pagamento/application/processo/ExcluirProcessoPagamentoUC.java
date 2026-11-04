package com.example.pagamento.application.processo;

import com.example.pagamento.adapter.database.ProcessoPagamentoRepository;
import com.example.pagamento.domain.valueobject.ProcessoPagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** DELETE do rascunho (só EM_EDICAO). Depois de submetido, o caminho é CANCELAR. Quem decide é o estado. */
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
