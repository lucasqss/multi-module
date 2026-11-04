package com.example.pagamento.application.pagamento;

import com.example.pagamento.adapter.database.PagamentoRepository;
import com.example.pagamento.domain.valueobject.PagamentoId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

/** DELETE físico: só rascunho (o estado decide). Depois de submetido, o caminho é cancelar. */
@ApplicationScoped
public class ExcluirPagamentoUC {

    private final PagamentoRepository repository;

    public ExcluirPagamentoUC(PagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void executar(UUID id) {
        var pagamentoId = new PagamentoId(id);
        repository.obterParaAlteracao(pagamentoId).validarExclusao();
        repository.remover(pagamentoId);
    }
}
